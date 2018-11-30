package com.doc.pychat;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends ArrayAdapter {

    List list = new ArrayList();

    public MessageAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    @Override
    public void add(@Nullable Object object) {
        super.add(object);
        list.add(object);
    }


    @Override
    public int getCount() {
        return list.size();
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View item;
        item = convertView;
        MessageHolder messageHolder;

        if (item == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            item = layoutInflater.inflate(R.layout.message_item, parent, false);
            messageHolder = new MessageHolder();
            messageHolder.userNameText = (TextView) item.findViewById(R.id.userTextView);
            messageHolder.textText = (TextView) item.findViewById(R.id.textTextView);
            item.setTag(messageHolder);
        } else {
            messageHolder = (MessageHolder) item.getTag();
        }
        Message message = (Message) this.getItem(position);

        String userName = String.valueOf(message.getUserName()) + ":";
        String text = String.valueOf(message.getText());


        messageHolder.userNameText.setText(userName);
        messageHolder.textText.setText(text);

        return item;
    }

    static class MessageHolder {
        TextView userNameText, textText;
    }
}