package com.kada;

import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

abstract public class EndlessAdapter extends BaseAdapter {

	private AtomicBoolean keepOnAppending=new AtomicBoolean(true);
	private ListAdapter wrapped;
	private View pendingView=null;
	private Context context;
	private int pendingResource=-1;
	abstract protected boolean cacheInBackground() throws Exception;
	abstract protected void appendCachedData();
	
	  public EndlessAdapter(ListAdapter wrapped) {
		  this.wrapped=wrapped;
		  }

	public EndlessAdapter(Context context, ListAdapter wrapped, int pendingResource){
		this.context=context;
		this.wrapped=wrapped;
		this.pendingResource=pendingResource;
	}
	
	@Override
	public int getCount() {
	    if (keepOnAppending.get()) {
	    	return(wrapped.getCount()+1);
	    }  
	    return(wrapped.getCount());
	}
  public int getItemViewType(int position) {
    if (position==getWrappedAdapter().getCount()) {
      return(IGNORE_ITEM_VIEW_TYPE);
    }
    
    return(super.getItemViewType(position));
  }
  public int getViewTypeCount() {
    return(wrapped.getViewTypeCount()+1);
  }

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return wrapped.getItem(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return wrapped.getItemId(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (position==wrapped.getCount() && keepOnAppending.get()) {
			if (pendingView==null) {
	            pendingView=getPendingView(parent);
	            new AppendTask().execute();
			}
			return(pendingView);
		}
		return(wrapped.getView(position, convertView, parent));
	}
	
	class AppendTask extends AsyncTask<Void, Void, Exception> {
		@Override
		protected Exception doInBackground(Void... params) {
			Exception result=null;
			try {
				keepOnAppending.set(cacheInBackground());
			}
		    catch (Exception e) {
		    	result=e;
		    }
		    return(result);
		}
		@Override
		protected void onPostExecute(Exception e) {
			if (e==null) {
				appendCachedData();
		     }
		     else {
		    	 Log.e("EndlessAdapter", "Exception in cacheInBackground()", e);
		    	 keepOnAppending.set(false);
		     } 
		     pendingView=null;
		     notifyDataSetChanged();
		 }
	  }

	  /**
	   * Inflates pending view using the pendingResource ID passed into the constructor
	   * @param parent
	   * @return inflated pending view, or null if the context passed into the pending view constructor was null.
	   */
	protected View getPendingView(ViewGroup parent) {
		if(context != null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			return inflater.inflate(pendingResource, parent, false);
		}
		throw new RuntimeException("You must either override getPendingView() or supply a pending View resource via the constructor");
	}
	
	protected ListAdapter getWrappedAdapter() {
	    return wrapped;
	}
}
