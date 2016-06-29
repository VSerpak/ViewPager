package com.example.vserp.viewpager.fragments;

import android.content.Context;
import android.database.Cursor;

import android.os.Bundle;

import android.support.annotation.Nullable;

import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import android.text.Editable;
import android.text.TextWatcher;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vserp.viewpager.R;
import com.example.vserp.viewpager.activities.MainActivity;
import com.example.vserp.viewpager.adapters.MyPagerAdapter;
import com.example.vserp.viewpager.services.MyIntentService;
import com.example.vserp.viewpager.utils.Prefs;
import com.example.vserp.viewpager.views.RoundChart;

/**
 * Created by vserp on 6/16/2016.
 */

public class CashFlowFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private int TAB_POSITION;
    private String CASH_FLOW_MONTHLY_FIELD_ITEM;
    private String CASH_FLOW_MONTHLY_FIELD_ITEM_PLAN;
    private String CURRENT_MONTH_ITEM;
    private String NEXT_MONTH_ITEM_PLAN;
    private String LAST_MONTH_ITEM;

    private String LAST_MONTH_EXPENSE = "last_month_expense";
    private String LAST_MONTH_INCOME = "last_month_income";
    private String CURRENT_MONTH_EXPENSE = "current_month_expense";
    private String CURRENT_MONTH_INCOME = "current_month_income";
    private String NEXT_MONTH_EXPENSE_PLAN = "next_month_expense_plan";
    private String NEXT_MONTH_INCOME_PLAN = "next_month_income_plan";

    private TextView tvLastMonth;
    private TextView tvCurrentMonth;
    private EditText etPlanNextMonth;

    public static String sPlan;
    public static String sCurrentPlan;

    private RoundChart rcCashFlow;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ((MainActivity)getActivity()).setFragmentInfo(MyPagerAdapter.FRAGMENT_INCOMES);

        final View view = inflater.inflate(R.layout.fragment_cash_flow, container, false);

        tvLastMonth = (TextView) view.findViewById(R.id.tv_last_month);
        tvCurrentMonth = (TextView) view.findViewById(R.id.tv_current_month);
        etPlanNextMonth = (EditText) view.findViewById(R.id.et_plan_next_month);
        rcCashFlow = (RoundChart) view.findViewById(R.id.rcRoundDiagram);

        etPlanNextMonth.setOnKeyListener(
                new View.OnKeyListener() {
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if (event.getAction() == KeyEvent.ACTION_DOWN &&
                                (keyCode == KeyEvent.KEYCODE_ENTER)) {
                            // сохраняем текст, введенный до нажатия Enter в переменную
                            sPlan = etPlanNextMonth.getText().toString();
                            MyIntentService.startActionAddIncomePlan(getActivity(), sPlan);

                            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            return true;
                        }
                        return false;
                    }
                }
        );
/*        etPlanNextMonth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (sPlan != ""){
                    sPlan = "0";
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                    sPlan = etPlanNextMonth.getText().toString();
                        MyIntentService.startActionAddIncomePlan(getActivity(), sPlan);
            }
        });*/

        getActivity().getSupportLoaderManager().initLoader(Prefs.ID_LOADER_INCOME_NAMES, null, this);

        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), Prefs.URI_CASH_FLOW_MONTHLY, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        naming(MainActivity.sTabPosition);

        if (data != null) {

            data.moveToFirst();

            if (data.getCount() != 0) {

                NEXT_MONTH_ITEM_PLAN = String.valueOf(data.getInt(data
                        .getColumnIndex(CASH_FLOW_MONTHLY_FIELD_ITEM_PLAN)));
                CURRENT_MONTH_ITEM = String.valueOf(data.getInt(data
                        .getColumnIndex(CASH_FLOW_MONTHLY_FIELD_ITEM)));

                //TODO selection of previous expenses and incomes

                LAST_MONTH_ITEM = String.valueOf(data.getInt(data
                        .getColumnIndex(CASH_FLOW_MONTHLY_FIELD_ITEM)));
            } else {
                NEXT_MONTH_ITEM_PLAN = "0";
                CURRENT_MONTH_ITEM = "0";
                LAST_MONTH_ITEM = "0";
            }
        }
        drawDiagram();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void drawDiagram() {

        int percentOfPlan;

        tvLastMonth.setText(LAST_MONTH_ITEM);
        tvCurrentMonth.setText(CURRENT_MONTH_ITEM);
        etPlanNextMonth.setText(NEXT_MONTH_ITEM_PLAN);

        if (!NEXT_MONTH_ITEM_PLAN.equals("0")) {
            if (Integer.parseInt(NEXT_MONTH_ITEM_PLAN) != 0) {
                percentOfPlan = Integer.parseInt(CURRENT_MONTH_ITEM)
                        * 100 / Integer.parseInt(NEXT_MONTH_ITEM_PLAN);
            } else {
                percentOfPlan = 0;
            }
            rcCashFlow.setValues(percentOfPlan);
            rcCashFlow.invalidate();
        }
    }

    private void naming(int position) {
        if (position == MyPagerAdapter.FRAGMENT_EXPENSES) {
            TAB_POSITION = MainActivity.sTabPosition;
            NEXT_MONTH_ITEM_PLAN = NEXT_MONTH_EXPENSE_PLAN;
            CURRENT_MONTH_ITEM = CURRENT_MONTH_EXPENSE;
            LAST_MONTH_ITEM = LAST_MONTH_EXPENSE;
            CASH_FLOW_MONTHLY_FIELD_ITEM_PLAN = Prefs.CASH_FLOW_MONTHLY_FIELD_EXPENSE_PLAN;
            CASH_FLOW_MONTHLY_FIELD_ITEM = Prefs.CASH_FLOW_MONTHLY_FIELD_EXPENSE;
        } else {
            TAB_POSITION = MainActivity.sTabPosition;
            NEXT_MONTH_ITEM_PLAN = NEXT_MONTH_INCOME_PLAN;
            CURRENT_MONTH_ITEM = CURRENT_MONTH_INCOME;
            LAST_MONTH_ITEM = LAST_MONTH_INCOME;
            CASH_FLOW_MONTHLY_FIELD_ITEM_PLAN = Prefs.CASH_FLOW_MONTHLY_FIELD_INCOME_PLAN;
            CASH_FLOW_MONTHLY_FIELD_ITEM = Prefs.CASH_FLOW_MONTHLY_FIELD_INCOME;
        }
    }
}
