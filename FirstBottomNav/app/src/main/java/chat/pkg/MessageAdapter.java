package chat.pkg;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.infiam.firstbottomnav.R;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Manab on 13-04-2019.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> mMessagesList;
    private FirebaseAuth mAuth;

    public MessageAdapter(List<Messages> mMessagesList){
        this.mMessagesList = mMessagesList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout,parent,false);
        return new MessageViewHolder(v);
    }

    @Override
    public int getItemCount() {
        return mMessagesList.size();
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {

        holder.messageImage.setImageDrawable(null);
        holder.messageText.setText(null);

        mAuth = FirebaseAuth.getInstance();
        String current_uid = mAuth.getCurrentUser().getUid();

        Messages c = mMessagesList.get(position);
        String from = c.getFrom();

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        if(from.equals(current_uid)){
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            setMessage(holder,c, R.drawable.message_sent_background,Color.BLACK, params);

        }
        else{
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            setMessage(holder,c,R.drawable.message_received_background,Color.WHITE, params);
        }

    }

    private void setMessage(MessageViewHolder holder, Messages c, int message_background, int color, RelativeLayout.LayoutParams params) {

        String type = c.getType();

        if(type.equals("text")){

            holder.messageText.setVisibility(View.VISIBLE);
            holder.messageImage.setVisibility(View.INVISIBLE);

            holder.messageText.setBackgroundResource(message_background);
            holder.messageText.setTextColor(color);
            holder.messageText.setLayoutParams(params);
            holder.messageText.setText(c.getMessage());
        }
        else if(type.equals("image")){

            holder.messageImage.setVisibility(View.VISIBLE);
            holder.messageText.setVisibility(View.INVISIBLE);

            holder.messageImage.setLayoutParams(params);
            holder.messageImage.setBackgroundResource(message_background);
            Picasso picasso = Picasso.get();
            picasso.setIndicatorsEnabled(false);
            //picasso.load(c.getThumb()).resize(600,600).centerCrop().into(holder.messageImage);
            picasso.load(c.getMessage()).resize(600,600).centerCrop().into(holder.messageImage);
        }

    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView messageText;
        public ImageView messageImage;

        public MessageViewHolder(View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message_single_text);
            messageImage = itemView.findViewById(R.id.message_single_image);
        }
    }

}