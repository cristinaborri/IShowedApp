package uk.ac.bbk.cristinaborri.ishowedapp;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import uk.ac.bbk.cristinaborri.ishowedapp.activity.EventViewActivity;

import static java.nio.charset.StandardCharsets.UTF_8;

public class AttendanceService {
    private static final String TAG = EventViewActivity.TAG;
    private static final Strategy STRATEGY = Strategy.P2P_STAR;

    private final EventViewActivity activity;
    private final DiscoveryOptions mDiscoveryOptions;
    private ConnectionsClient mConnectionsClient;
    private String currentEndpointId;
    private String serviceName;

    public AttendanceService(EventViewActivity targetActivity, String eventName) {
        activity = targetActivity;

        serviceName = eventName;

        mConnectionsClient = Nearby.getConnectionsClient(activity);

        mDiscoveryOptions = (new DiscoveryOptions.Builder()).setStrategy(STRATEGY).build();
    }

    // Callbacks for receiving payloads
    private final PayloadCallback mPayloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload payload) {
                    Log.i(TAG, "connection: payload received");
                    // remove event
                    activity.registrationConfirmed();
                }

                @Override
                public void onPayloadTransferUpdate(@NonNull String endpointId, @NonNull PayloadTransferUpdate update) { }
            };

    // Callbacks for connections to other devices
    private final ConnectionLifecycleCallback mConnectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo connectionInfo) {
                    Log.i(TAG, "connection: accepting connection, id: "+endpointId);
                    // Automatically accept the connection on both sides.
                    if (currentEndpointId != null) {
                        mConnectionsClient.disconnectFromEndpoint(currentEndpointId);
                        currentEndpointId = null;
                    }
                    mConnectionsClient.acceptConnection(endpointId, mPayloadCallback);
                }

                @Override
                public void onConnectionResult(@NonNull String endpointId, ConnectionResolution result) {
                    if (result.getStatus().isSuccess()) {
                        Log.i(TAG, "connection: connection successful, id:" + endpointId);
                        currentEndpointId = endpointId;
                        activity.showAttendanceButton();
                    } else {
                        Log.i(TAG, "connection: connection failed");
                    }
                }

                @Override
                public void onDisconnected(@NonNull String endpointId) {
                    Log.i(TAG, "connection: disconnected");
                    if (currentEndpointId.equals(endpointId)) {
                        currentEndpointId = null;
                    }
                    activity.showSearchService();
                    restartDiscovery();
                }
            };

    private final EndpointDiscoveryCallback mEndpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(@NonNull String endpointId, DiscoveredEndpointInfo discoveredEndpointInfo)
                {
                    // An endpoint was found!
                    mConnectionsClient.requestConnection(
                            discoveredEndpointInfo.getEndpointName(),
                            endpointId,
                            mConnectionLifecycleCallback)
                            .addOnSuccessListener(
                                    new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unusedResult) {
                                            Log.i(TAG, "connection: connecting endpoint");
                                        }
                                    })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.i(TAG, "connection: endpoint connection error: " + e.getMessage());
                                            restartDiscovery();
                                        }
                                    });
                }

                @Override
                public void onEndpointLost(@NonNull String endpointId) {
                    Log.i(TAG, "connection: endpoint lost");
                }
            };

    public void restartDiscovery() {
        // Starting the discovery if already started raises an error stopping is safe
        stopDiscovery();
        mConnectionsClient.startDiscovery(
                serviceName,
                mEndpointDiscoveryCallback,
                mDiscoveryOptions)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unusedResult) {
                                // We're discovering!
                                Log.i(TAG, "connection: discovery started");

                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // We were unable to start discovering.
                                Log.i(TAG, "connection: discovery failed: " + e.getMessage());
                            }
                        });
    }

    public void stopDiscovery() {
        if (currentEndpointId != null) {
            mConnectionsClient.disconnectFromEndpoint(currentEndpointId);
        }
        mConnectionsClient.stopDiscovery();
        Log.i(TAG, "connection: discovery stopped");
    }

    public void registerAttendance(String code)
    {
        if (currentEndpointId != null) {
            mConnectionsClient.sendPayload(currentEndpointId, Payload.fromBytes(code.getBytes(UTF_8)));
            Log.i(TAG, "connection: payload sent");
        }
    }
}
