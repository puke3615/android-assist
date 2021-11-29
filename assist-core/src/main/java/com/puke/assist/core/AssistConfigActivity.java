package com.puke.assist.core;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.puke.assist.core.model.ConfigModel;
import com.puke.assist.core.model.PropertyModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Config setting page
 *
 * @author puke
 * @version 2021/9/9
 */
public class AssistConfigActivity extends Activity {

    private static final int TYPE_CONFIG = 0;
    private static final int TYPE_PROP_INPUT = 1;
    private static final int TYPE_PROP_BOOLEAN = 2;
    private static final int TYPE_PROP_OPTION = 3;

    private static final List<Integer> PROPERTY_ORDER = Arrays.asList(
            TYPE_PROP_INPUT,
            TYPE_PROP_BOOLEAN,
            TYPE_PROP_OPTION
    );

    private final List<ItemData> dataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_assist_config);

        RecyclerView recyclerView = findViewById(R.id.assist_config_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        dataList.addAll(loadData(this));
        recyclerView.setAdapter(new Adapter());

        findViewById(R.id.assist_config_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.assist_config_commit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCommit();
            }
        });
    }

    private void onCommit() {
        boolean needReboot = false;
        List<SaveItem> updatedItems = new ArrayList<>();
        for (ItemData itemData : dataList) {
            if (itemData.type == TYPE_CONFIG) {
                continue;
            }

            PropertyModel propertyModel = itemData.propertyModel;
            String oldValue = propertyModel.value;
            String newValue = propertyModel.currentValue;
            if (TextUtils.equals(newValue, oldValue)) {
                continue;
            }

            ConfigModel configModel = itemData.configModel;
            updatedItems.add(new SaveItem(configModel.id, propertyModel.id, newValue));
            if (propertyModel.rebootIfChanged) {
                needReboot = true;
            }
        }

        if (updatedItems.isEmpty()) {
            Toast.makeText(this, R.string.no_config_updated, Toast.LENGTH_SHORT).show();
            return;
        }

        if (needReboot) {
            new AlertDialog.Builder(this)
                    .setTitle(null)
                    .setMessage(R.string.reboot_tips)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            doSave(updatedItems);
                            relaunchApp(AssistConfigActivity.this);
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        } else {
            doSave(updatedItems);
            finish();
        }
    }

    private void doSave(List<SaveItem> saveItems) {
        for (SaveItem item : saveItems) {
            SpHelper.putString(this, item.spName, item.spKey, item.spValue);
        }
    }

    private void relaunchApp(Context context) {
        getWindow().getDecorView().postDelayed(new Runnable() {
            @Override
            public void run() {
                final Intent intent = context.getPackageManager()
                        .getLaunchIntentForPackage(context.getPackageName());
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(intent);
                }
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
            }
        }, 200);
    }

    private static List<ItemData> loadData(Context context) {
        List<ConfigModel> configModels = ConfigManager.fetchConfigData(context);
        List<ItemData> dataList = new ArrayList<>();
        for (ConfigModel configModel : configModels) {
            if (!configModel.hasProperties()) {
                continue;
            }

            // Config item
            dataList.add(new ItemData(TYPE_CONFIG, configModel, null));

            // Property item
            List<ItemData> properties = new ArrayList<>();
            for (PropertyModel propertyModel : configModel.properties) {
                Class<?> propertyType = propertyModel.type;
                if (propertyType == Boolean.class || propertyType == boolean.class) {
                    properties.add(new ItemData(TYPE_PROP_BOOLEAN, configModel, propertyModel));
                } else if (Util.isNotEmpty(propertyModel.options)) {
                    properties.add(new ItemData(TYPE_PROP_OPTION, configModel, propertyModel));
                } else {
                    properties.add(new ItemData(TYPE_PROP_INPUT, configModel, propertyModel));
                }
            }
            Collections.sort(properties, new Comparator<ItemData>() {
                @Override
                public int compare(ItemData o1, ItemData o2) {
                    return PROPERTY_ORDER.indexOf(o1.type) - PROPERTY_ORDER.indexOf(o2.type);
                }
            });
            dataList.addAll(properties);
        }
        return dataList;
    }

    private class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == TYPE_CONFIG) {
                View view = inflater.inflate(R.layout.item_config, parent, false);
                return new ConfigHolder(view);
            } else if (viewType == TYPE_PROP_INPUT) {
                View view = inflater.inflate(R.layout.item_property_input, parent, false);
                return new PropertyInputHolder(view);
            } else if (viewType == TYPE_PROP_BOOLEAN) {
                View view = inflater.inflate(R.layout.item_property_switch, parent, false);
                return new PropertySwitchHolder(view);
            } else if (viewType == TYPE_PROP_OPTION) {
                View view = inflater.inflate(R.layout.item_property_option, parent, false);
                return new PropertyOptionHolder(view);
            }
            throw new RuntimeException("Unknown view type: " + viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            int itemViewType = getItemViewType(position);
            ItemData itemData = dataList.get(position);
            ConfigModel configModel = itemData.configModel;
            PropertyModel propertyModel = itemData.propertyModel;
            switch (itemViewType) {
                case TYPE_CONFIG:
                    ConfigHolder configHolder = (ConfigHolder) holder;
                    configHolder.name.setText(configModel.name);
                    break;
                case TYPE_PROP_INPUT:
                    PropertyInputHolder inputHolder = (PropertyInputHolder) holder;
                    inputHolder.tips.setText(getTips(propertyModel));

                    EditText editText = inputHolder.input;
                    Object inputTag = editText.getTag();
                    if ((inputTag instanceof TextWatcher)) {
                        editText.removeTextChangedListener(((TextWatcher) inputTag));
                    }

                    String currentValue = propertyModel.currentValue;
                    if (!TextUtils.isEmpty(currentValue)
                            && TextUtils.equals(propertyModel.defaultValue, currentValue)
                            && propertyModel.hideDefaultText) {
                        editText.setText(convertHideTextContent(currentValue));
                        holder.itemView.setTag(R.id.tag_hide_default_text, true);
                    } else {
                        editText.setText(currentValue);
                    }

                    TextWatcher watcher = new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            Object tagOfItemView = holder.itemView.getTag(R.id.tag_hide_default_text);
                            boolean currentIsHideDefaultText = Boolean.TRUE.equals(tagOfItemView);
                            if (currentIsHideDefaultText) {
                                final TextWatcher thisWatcher = this;
                                AlertDialog dialog = new AlertDialog.Builder(AssistConfigActivity.this)
                                        .setTitle(null)
                                        .setMessage("确认更改默认配置信息吗？")
                                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                holder.itemView.setTag(R.id.tag_hide_default_text, false);
                                                editText.setText(null);
                                            }
                                        })
                                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                editText.removeTextChangedListener(thisWatcher);
                                                String hideTextContent = convertHideTextContent(propertyModel.defaultValue);
                                                editText.setText(hideTextContent);
                                                editText.setSelection(hideTextContent.length());
                                                editText.addTextChangedListener(thisWatcher);
                                            }
                                        })
                                        .create();
                                dialog.setCanceledOnTouchOutside(false);
                                dialog.setCancelable(false);
                                dialog.show();
                            } else {
                                propertyModel.currentValue = s.toString();
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                        }
                    };
                    editText.setTag(watcher);
                    editText.addTextChangedListener(watcher);
                    break;
                case TYPE_PROP_BOOLEAN:
                    PropertySwitchHolder switchHolder = (PropertySwitchHolder) holder;
                    switchHolder.tips.setText(getTips(propertyModel));

                    boolean checked = Boolean.parseBoolean(propertyModel.currentValue);
                    switchHolder.switcher.setOnCheckedChangeListener(null);
                    switchHolder.switcher.setChecked(checked);
                    switchHolder.switcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            propertyModel.currentValue = String.valueOf(isChecked);
                        }
                    });
                    break;
                case TYPE_PROP_OPTION:
                    PropertyOptionHolder optionHolder = (PropertyOptionHolder) holder;
                    optionHolder.tips.setText(getTips(propertyModel));

                    optionHolder.spinner.setOnItemSelectedListener(null);
                    final List<String> options = propertyModel.options;
                    final List<String> enumTipsOptions = propertyModel.enumTipsOptions;
                    optionHolder.spinner.setAdapter(new ArrayAdapter<>(
                            AssistConfigActivity.this,
                            android.R.layout.simple_spinner_dropdown_item,
                            Util.isNotEmpty(enumTipsOptions) ? enumTipsOptions : options
                    ));
                    int currentIndex = options.indexOf(propertyModel.currentValue);
                    optionHolder.spinner.setSelection(Math.max(currentIndex, 0));
                    optionHolder.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            propertyModel.currentValue = options.get(position);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                    break;
            }
        }

        private String convertHideTextContent(String currentValue) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < currentValue.length(); i++) {
                builder.append("*");
            }
            return builder.toString();
        }

        private String getTips(PropertyModel propertyModel) {
            String tips = propertyModel.tips;
            if (TextUtils.isEmpty(tips)) {
                tips = propertyModel.id;
            }
            return tips;
        }

        @Override
        public int getItemViewType(int position) {
            return dataList.get(position).type;
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }
    }

    private static class ConfigHolder extends RecyclerView.ViewHolder {
        final TextView name;

        public ConfigHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.item_name);
        }
    }

    private static class PropertyInputHolder extends RecyclerView.ViewHolder {
        final TextView tips;
        final EditText input;

        public PropertyInputHolder(@NonNull View itemView) {
            super(itemView);
            tips = itemView.findViewById(R.id.item_tips);
            input = itemView.findViewById(R.id.item_input);
        }
    }

    private static class PropertySwitchHolder extends RecyclerView.ViewHolder {
        final TextView tips;
        final Switch switcher;

        public PropertySwitchHolder(@NonNull View itemView) {
            super(itemView);
            tips = itemView.findViewById(R.id.item_tips);
            switcher = itemView.findViewById(R.id.item_switcher);
        }
    }

    private static class PropertyOptionHolder extends RecyclerView.ViewHolder {
        final TextView tips;
        final Spinner spinner;

        public PropertyOptionHolder(@NonNull View itemView) {
            super(itemView);
            tips = itemView.findViewById(R.id.item_tips);
            spinner = itemView.findViewById(R.id.item_spinner);
        }
    }

    private static class ItemData {
        final int type;
        final ConfigModel configModel;
        final PropertyModel propertyModel;

        ItemData(int type, ConfigModel configModel, PropertyModel propertyModel) {
            this.type = type;
            this.configModel = configModel;
            this.propertyModel = propertyModel;
        }
    }

    private static class SaveItem {
        final String spName;
        final String spKey;
        final String spValue;

        SaveItem(String spName, String spKey, String spValue) {
            this.spName = spName;
            this.spKey = spKey;
            this.spValue = spValue;
        }
    }
}
