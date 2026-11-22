package com.farm.seedadviosory.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.farm.seedadviosory.R;
import com.farm.seedadviosory.data.mock.MockDataProvider;
import com.farm.seedadviosory.data.model.NotificationItem;

import java.util.List;

public class NotificationsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayout container = view.findViewById(R.id.layout_notifications);
        List<NotificationItem> notifications = MockDataProvider.getDemoNotifications();

        for (NotificationItem notification : notifications) {
            View card = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_notification, container, false);

            ((TextView) card.findViewById(R.id.tv_title)).setText(notification.getTitle());
            ((TextView) card.findViewById(R.id.tv_message)).setText(notification.getMessage());
            ((TextView) card.findViewById(R.id.tv_timestamp)).setText(notification.getTimestamp());

            container.addView(card);
        }
    }
}
