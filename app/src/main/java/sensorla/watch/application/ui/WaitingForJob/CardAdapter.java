package sensorla.watch.application.ui.WaitingForJob;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import sensorla.watch.application.MainActivity;
import sensorla.watch.application.R;
import sensorla.watch.application.ui.ReceivingWO.ReceiveWOFragment;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {
    private Context context;
    public List<CardInfo> cardList;

    public CardAdapter(Context context, List<CardInfo> cardList) {
        this.context = context;
        this.cardList = cardList;
    }

    public void updateCardList(List<CardInfo> cardList) {
        this.cardList = cardList;
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card, viewGroup, false);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        return new CardViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final CardAdapter.CardViewHolder holder, int i) {
        final CardInfo cardinfo = cardList.get(i);

        holder.vName.setText(cardinfo.name);
        holder.vlocation.setText(cardinfo.location);
        holder.vStatus.setText (cardinfo.status);
        if(cardinfo.type == "adhoc")
        {
            holder.vTitle.setImageResource(R.drawable.adhoc);
        }
        else if(cardinfo.type == "schedule")
        {
            holder.vTitle.setImageResource(R.drawable.schedule);
        }
        else if(cardinfo.type == "People Count")
        {
            holder.vTitle.setImageResource(R.drawable.ppl_count);
        }
        else if(cardinfo.type == "Ammonia 1" || cardinfo.type == "Ammonia 2")
        {
            holder.vTitle.setImageResource(R.drawable.ammonia);
        }
        else
        {
            holder.vTitle.setImageResource(R.drawable.adhoc);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity main = (MainActivity)context;
                Fragment fragment = new ReceiveWOFragment();

                Gson gson = new Gson();
                String cardInfoString = gson.toJson(cardinfo);

                Bundle bundle = new Bundle();
                bundle.putString("cardInfo", cardInfoString);

                fragment.setArguments(bundle);
                FragmentTransaction transaction = main.getSupportFragmentManager()
                        .beginTransaction();
                transaction.replace(R.id.nav_host_fragment, fragment,"clear");
                transaction.commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }

    public class CardViewHolder extends RecyclerView.ViewHolder{

        protected TextView vName;
        protected TextView vStatus;
        protected TextView vlocation;
        protected ImageView vTitle;

        public CardViewHolder(View v) {
            super(v);
            vName = v.findViewById(R.id.txtName);
            vlocation =v.findViewById(R.id.txtLocation);
            vTitle =v.findViewById(R.id.human);
            vStatus = v.findViewById(R.id.txtstatus);
        }
    }

}
