package com.example.omgups;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RadioButton;
import android.widget.TextView;

public class ExpListAdapter extends BaseExpandableListAdapter implements Filterable {
	/**
	 * Кастомный адаптер, используется для отображения списка групп и преподавателей
	 * Совмещает в себе возможность выбора списка групп и основной группы
	 */
	private ArrayFilter mFilter;
	private ArrayList<ArrayList<Model>> mGroup;
	private Context mContext;
	private ArrayList<String> mNames;
	private final Object mLock = new Object();
	private ArrayList<ArrayList<Model>> mOriginalValues;
	private ArrayList<String> mOriginalNames;
	SharedPreferences sPref;
	Editor ed;


	public ExpListAdapter (Context context, ArrayList<String> names, 
			ArrayList<ArrayList<Model>> groups){
		sPref = context.getSharedPreferences("groups", Context.MODE_PRIVATE);
		ed = sPref.edit();
		mContext = context;
		mNames = names;
		mGroup = groups;
		Set<String> oldList = sPref.getStringSet("listId", new HashSet<String>()); //Список старых групп
		String oldMain = sPref.getString("main_group_id", "");
		for (int i = 0; i < mNames.size(); i++)
			for (int j = 0; j < mGroup.get(i).size(); j++) {
				if (oldList.contains(mGroup.get(i).get(j).getId())) {
					mGroup.get(i).get(j).setMarked(true);
				}
				if (mGroup.get(i).get(j).getId().equals(oldMain)) {
					mGroup.get(i).get(j).setSelected(true);
				}
			}
	}

	@Override
	public int getGroupCount() {
		return mGroup.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return mGroup.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return mGroup.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return mGroup.get(groupPosition).get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	static class Holder {
		public TextView textGroup;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
			ViewGroup parent) {	

		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.group_view, null);
		}

		if (isExpanded){
			//Изменяем что-нибудь, если текущая Group раскрыта
		}
		else{
			//Изменяем что-нибудь, если текущая Group скрыта
		}

		TextView textGroup = (TextView) convertView.findViewById(R.id.textGroup);
		textGroup.setText(mNames.get(groupPosition));

		return convertView;

	}

	static class ViewHolder {
		public RadioButton radioButton;
		public TextView textView;
	}

	@Override
	public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild,
			View convertView, ViewGroup parent) {
		View view = null;
		//				if (convertView == null) {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.child_view, null);
		if (mGroup.get(groupPosition).get(childPosition).isMarked()) {
			view.setBackgroundColor(mContext.getResources().getColor(R.color.schedule_light));
		}
		final ViewHolder holder = new ViewHolder();
		holder.textView = (TextView) view.findViewById(R.id.textChild);
		holder.radioButton = (RadioButton) view.findViewById(R.id.buttonChild);
		holder.radioButton.setChecked(mGroup.get(groupPosition).get(childPosition).isSelected());
		//		rb =  holder.radioButton;

		holder.radioButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override //При нажатии на radioButton. Такой элемент считается основным
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				for (int i = 0; i < mNames.size(); i++)
					for (int j = 0; j < mGroup.get(i).size(); j++) {
						if (!(i == groupPosition && j == childPosition))
							mGroup.get(i).get(j).setSelected(false);
					}
				Model element = (Model) holder.radioButton.getTag();
				element.setSelected(buttonView.isChecked());
				element.setMarked(buttonView.isChecked());
				notifyDataSetChanged();
			}

		});
		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!mGroup.get(groupPosition).get(childPosition).isSelected()) {
					if (!mGroup.get(groupPosition).get(childPosition).isMarked()) {
						view.setBackgroundColor(mContext.getResources().getColor(R.color.schedule_light));
						mGroup.get(groupPosition).get(childPosition).setMarked(true);
					}
					else {
						view.setBackgroundColor(Color.WHITE);
						mGroup.get(groupPosition).get(childPosition).setMarked(false);
					}
				}
			}
		});
		view.setTag(holder);
		holder.radioButton.setTag(mGroup.get(groupPosition).get(childPosition));
		ViewHolder hholder = (ViewHolder) view.getTag();
		hholder.textView.setText(mGroup.get(groupPosition).get(childPosition).getName());
		hholder.radioButton.setChecked(mGroup.get(groupPosition).get(childPosition).isSelected());
		return view;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	public Model mainGroup() {
		for (int i = 0; i < mNames.size(); i++)
			for (int j = 0; j < mGroup.get(i).size(); j++) {
				if (mGroup.get(i).get(j).isSelected()) {
					return mGroup.get(i).get(j);
				}
			}
		return null;
	}

	public ArrayList<Model> groups() {
		ArrayList<Model> list = new ArrayList<Model>();
		for (int i = 0; i < mNames.size(); i++)
			for (int j = 0; j < mGroup.get(i).size(); j++) {
				if (mGroup.get(i).get(j).isSelected() || mGroup.get(i).get(j).isMarked()) {
					list.add(mGroup.get(i).get(j));
				}
			}
		return list;
	}

	@Override
	public Filter getFilter() {
		if (mFilter == null) {
			mFilter = new ArrayFilter();
		}
		return mFilter;
	}

	private class ArrayFilter extends Filter {
		FilterResults resultsNames = new FilterResults();

		@SuppressLint("DefaultLocale")
		@Override
		protected FilterResults performFiltering(CharSequence prefix) {
			FilterResults results = new FilterResults();

			if (mOriginalValues == null) {
				synchronized (mLock) {
					mOriginalValues = new ArrayList<ArrayList<Model>>(mGroup);
					mOriginalNames = new ArrayList<String>(mNames);
				}
			}
			if (prefix == null || prefix.length() == 0) {
				ArrayList<ArrayList<Model>> list;
				ArrayList<String> names;
				synchronized (mLock) {
					list = new ArrayList<ArrayList<Model>>(mOriginalValues);
					names = new ArrayList<String>(mOriginalNames);
				}
				results.values = list;
				results.count = list.size();
				resultsNames.values = names;
				resultsNames.count = names.size();
			} else {
				String prefixString = prefix.toString().replaceAll(" ", "").toLowerCase();


				ArrayList<ArrayList<Model>> values;
				synchronized (mLock) {
					values = new ArrayList<ArrayList<Model>>(mOriginalValues);
				}


				final ArrayList<ArrayList<Model>> newValues = new ArrayList<ArrayList<Model>>();
				final ArrayList<String> newNames = new ArrayList<String>();

				for (int i = 0; i < values.size(); i++) {
					final int count = values.get(i).size();
					final ArrayList<Model> newValuesChild = new ArrayList<Model>();
					for (int j = 0; j < count; j++) {
						final Model value = values.get(i).get(j);
						final String valueText = value.getName().toString().replaceAll(" ", "").toLowerCase(Locale.getDefault());

						// First match against the whole, non-splitted value
						if (valueText.startsWith(prefixString)) {
							newValuesChild.add(value);
						}
						else {     //кусок не починен, теоретически определяет содержание вообще в тексте, а не с начала
							final String[] words = valueText.split(" ");
							final int wordCount = words.length;

							// Start at index 0, in case valueText starts with space(s)
							for (int k = 0; k < wordCount; k++) {
								if (words[k].startsWith(prefixString)) {
									newValuesChild.add(value);
									break;
								}
							}
						}
					}
					if (newValuesChild.size() > 0) {
						newValues.add(newValuesChild);
						newNames.add(mOriginalNames.get(i));
					}
				}

				results.values = newValues;
				results.count = newValues.size();
				resultsNames.values = newNames;
				resultsNames.count = newNames.size();

			}

			return results;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			//noinspection unchecked
			mGroup = (ArrayList<ArrayList<Model>>) results.values;
			mNames = (ArrayList<String>) resultsNames.values;
			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}
	}
}