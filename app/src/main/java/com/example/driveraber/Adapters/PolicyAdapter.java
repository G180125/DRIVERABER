package com.example.driveraber.Adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.driveraber.Models.Staff.Policy;
import com.example.driveraber.R;

import java.util.List;

public class PolicyAdapter extends RecyclerView.Adapter<PolicyAdapter.PolicyViewHolder>{
    private List<Policy> policyList;

    public PolicyAdapter(List<Policy> policyList) {
        this.policyList = policyList;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setPolicyList(List<Policy> policyList) {
        this.policyList = policyList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PolicyAdapter.PolicyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.policy_detail, parent, false);
        return new PolicyAdapter.PolicyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PolicyAdapter.PolicyViewHolder holder, int position) {
        Policy policy = policyList.get(position);
        holder.bind(policy, position);
    }

    @Override
    public int getItemCount() {
        return policyList.size();
    }

    public class PolicyViewHolder extends RecyclerView.ViewHolder{
        TextView policyTextView;
        public PolicyViewHolder(@NonNull View itemView) {
            super(itemView);
            policyTextView = itemView.findViewById(R.id.policy);
        }

        public void bind(Policy policy, int position) {
            policyTextView.setText(policy.getPolicy());
        }

    }
}
