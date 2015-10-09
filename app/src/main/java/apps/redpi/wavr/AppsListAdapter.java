package apps.redpi.wavr;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Nikhil on 09-10-2015.
 */
public class AppsListAdapter extends BaseAdapter {
    ArrayList<AppData> appList;
    Activity mActivity;

    public AppsListAdapter(ArrayList<AppData> list,Activity activity){
        appList = list;
        mActivity = activity;
    }
    @Override
    public int getCount() {
        return appList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    private class ViewHolder{
        private TextView appName;
        private ImageView appLogo;

    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        AppData appData = appList.get(i);
        if(view==null){
            holder = new ViewHolder();
            view = mActivity.getLayoutInflater().inflate(R.layout.apps_list_item,null);
            holder.appLogo = (ImageView) view.findViewById(R.id.appImage);
            holder.appName = (TextView) view.findViewById(R.id.appName);
            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }

        holder.appName.setText(appData.appName);
        holder.appLogo.setImageDrawable(appData.appLogo);

        return view;
    }
}
