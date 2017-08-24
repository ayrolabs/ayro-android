package io.chatz.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

  private Context context;
  private List<T> items;
  private boolean reversed;

  public BaseAdapter(Context context, List<T> items) {
    this.context = context;
    this.items = new ArrayList<>(items);
  }

  public boolean isReversed() {
    return reversed;
  }

  public void setReversed(boolean reversed) {
    this.reversed = reversed;
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  public Context getContext() {
    return context;
  }

  public List<T> getItems() {
    return items;
  }

  public void setItems(List<T> items) {
    this.items = new ArrayList<>(items);
    notifyDataSetChanged();
  }

  public T getItem(int index) {
    return items.get(index);
  }

  public int addItem(T item) {
    return addItem(item, reversed);
  }

  public int addItem(T item, boolean reversed) {
    if (!reversed) {
      items.add(item);
      notifyItemInserted(items.size() - 1);
      return items.size() - 1;
    } else {
      getItems().add(0, item);
      notifyItemInserted(0);
      return 0;
    }
  }

  public void addItem(int index, T item) {
    items.add(index, item);
    notifyItemInserted(index);
  }

  public void addItems(List<T> items) {
    int index = this.items.size();
    addItems(index, items);
  }

  public void addItems(int index, List<T> items) {
    this.items.addAll(index, items);
    notifyItemRangeInserted(index, items.size());
  }

  public void removeItem(T item) {
    int index = items.indexOf(item);
    if (index >= 0) {
      removeItem(index);
    }
  }

  public void removeItem(int index) {
    items.remove(index);
    notifyItemRemoved(index);
  }

  public void removeItems(List<T> items) {
    removeItems(items, true);
  }

  public void removeItems(List<T> items, boolean notify) {
    this.items.removeAll(items);
    if (notify) {
      notifyDataSetChanged();
    }
  }

  public void clearItems() {
    items.clear();
    notifyDataSetChanged();
  }

  public void reloadItems() {
    notifyDataSetChanged();
  }

  public void reloadItem(int index) {
    notifyItemChanged(index);
  }

  public boolean isEmpty() {
    return items.isEmpty();
  }

  public int lastIndex() {
    return getItemCount() - 1;
  }

  public boolean hasItem(int index) {
    return index >= 0 && index < getItemCount();
  }
}