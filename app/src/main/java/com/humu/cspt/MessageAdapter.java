package com.humu.cspt;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * @author humu
 * @since 2020/3/2
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private final Context context;
    private final List<String> msgs;

    public MessageAdapter(Context context, List<String> msgs) {
        this.context = context;
        this.msgs = msgs;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = View.inflate(context, R.layout.item_message, null);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder messageViewHolder, int i) {
        messageViewHolder.tvMsg.setText(msgs.get(i));
    }

    @Override
    public int getItemCount() {
        return (msgs != null && msgs.size() > 0) ? msgs.size() : 0;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvMsg;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMsg = itemView.findViewById(R.id.tv_msg);
        }
    }

    public void addMsg(String msg){
        if(TextUtils.isEmpty(msg)){
            return;
        }
        msgs.add(msg);
        notifyDataSetChanged();
    }

}
