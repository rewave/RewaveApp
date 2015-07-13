package com.rewaveapp;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import org.w3c.dom.Text;

import java.util.List;

public class ListerAdapter extends BaseAdapter {

    private Context context;
    private  List<BluetoothDevice> deviceList;
    private String deviceName;
    private ViewHolder holder;
    private LayoutInflater inflater;
    ColorGenerator generator = ColorGenerator.MATERIAL;
    TextDrawable.IBuilder builder = TextDrawable.builder()
            .beginConfig()
            .endConfig()
            .round()
    ;
    static class ViewHolder {
        ImageView letterTile;
        TextView deviceName;
        int color = 0;
    }

    public ListerAdapter(Context context, List<BluetoothDevice> deviceList) {
        this.context = context;
        this.deviceList = deviceList;
        inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return deviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return deviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup container) {
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.lister_item, null);
            holder.letterTile = (ImageView) convertView.findViewById(R.id.letter_tile);
            holder.deviceName = (TextView) convertView.findViewById(R.id.device_name);
            holder.color = generator.getRandomColor();
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        deviceName = ((BluetoothDevice) getItem(position)).getName();
        holder.letterTile.setImageDrawable(builder.build(deviceName.substring(0, 1), holder.color != 0 ? holder.color : 1));
        holder.deviceName.setText(deviceName);

        return convertView;
    }
}
