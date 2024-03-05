package com.example.skyscripts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class WeatherRVadapter extends  RecyclerView.Adapter<WeatherRVadapter.ViewHolder> {
private Context context;
private ArrayList<WeatherRV> weatherRvArrayList;

public WeatherRVadapter(Context context, ArrayList<WeatherRV> weatherRvArrayList) {
        this.context = context;
        this.weatherRvArrayList = weatherRvArrayList;
        }

@NonNull
@Override
public WeatherRVadapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.weatherrvitem,parent,false);
        return new ViewHolder(view);
        }

        @NonNull
        @SuppressLint("SetTextI18n")
@Override
public void onBindViewHolder(@NonNull WeatherRVadapter.ViewHolder holder, int position) {
        WeatherRV modal = weatherRvArrayList.get(position);
        holder.temptv.setText(modal.getTemperature()+"°c");
        holder.windtv.setText(modal.getWindspeed()+"KmPh");
        Picasso.get().load("http:".concat(modal.getIcon())).into(holder.conditioniv);
@SuppressLint("SimpleDateFormat") SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd hh:mm");
@SuppressLint("SimpleDateFormat") SimpleDateFormat output = new SimpleDateFormat("hh:mm aa");
        try{
        Date t = input.parse(modal.getTime());
        holder.timetv.setText(output.format(t));
        }catch (ParseException e){
        e.printStackTrace();
        }

        }

@Override
public int getItemCount() {
        return weatherRvArrayList.size();
        }
        public class ViewHolder extends RecyclerView.ViewHolder {
                private TextView timetv,windtv,temptv;
                private ImageView conditioniv;
                public ViewHolder(@NonNull View itemView) {
                        super(itemView);
                        timetv = itemView.findViewById(R.id.timetv);
                        windtv = itemView.findViewById(R.id.windspeed);
                        temptv = itemView.findViewById(R.id.temperaturetv);
                        conditioniv = itemView.findViewById(R.id.ivcondition);

                }
        }
        }

