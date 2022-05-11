package cnam.smb116.smb116_tp11;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class DNSSDDiscover {

    private static final String TAG = "DNSSDDiscover";
    private final String SERVICE_NAME = "Deptinfo";
    private final String SERVICE_TYPE = "_http._tcp.";
    private final Context context;
    private final String serviceNameDiscover = "test";
    private NsdManager nsdManager2;
    private NsdManager.DiscoveryListener discoveryListener;
    private NsdManager.ResolveListener resolveListener;
    private Integer port;
    private InetAddress host;
    private final MainActivity mainActivity;

    public DNSSDDiscover(Context context, MainActivity mainActivity){
        this.context = context;
        this.mainActivity = mainActivity;

        initializeResolveListener();
        initializeDiscoveryListener();
    }

    public void startDiscovery(){
        discoverServices();
    }

    public void stopDiscovery(){
        nsdManager2.stopServiceDiscovery(discoveryListener);
    }

    public void sendMessage(){
        showText("connecting...");
        if (port != null && host != null){
            showText("sending message...");
            buildSocketMessage();
        }else{
            showText("Connecting problem !");
        }
    }

    public void initializeDiscoveryListener() {
        discoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
                showText("onDiscoveryStarted...");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                Log.d(TAG, "Service discovery success" + service);
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());

                } else if (service.getServiceName().equals(serviceNameDiscover)) {
                    Log.d(TAG, "Same machine: " + serviceNameDiscover);
                } else if (service.getServiceName().contains(SERVICE_NAME)){
                    initializeResolveListener();
                    nsdManager2.resolveService(service, resolveListener);
                    showText("onServiceFound: "+service.getServiceName()+"("+service.getServiceType()+")");
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Log.e(TAG, "service lost: " + service);
                showText("onServiceLost: "+service.getServiceName());
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
                showText("onDiscoveryStopped: " + serviceType);
                showText("Discovery stopped: ");
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                showText("Discovery failed: Error code:" + errorCode);
                nsdManager2.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                showText("Discovery failed: Error code:" + errorCode);
                nsdManager2.stopServiceDiscovery(this);
            }
        };
    }

    public void initializeResolveListener() {
        resolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "Resolve failed: " + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

                if (serviceInfo.getServiceName().equals(serviceNameDiscover)) {
                    Log.d(TAG, "Same IP.");
                    showText("Same IP.");
                    return;
                }
                port = serviceInfo.getPort();
                host = serviceInfo.getHost();

                Log.i(TAG, "it works");
                showText("onServiceResolved: "+ serviceInfo.getServiceName()+" (port: "+port+", address: "+host+")");
            }
        };
    }


    public void discoverServices(){
        nsdManager2 = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        nsdManager2.discoverServices(
                SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
        showText("Discovering started...");
    }

    public void showText(String texte){
        mainActivity.runOnUiThread(() -> mainActivity.callDiscoverUI(texte));
    }

    public void buildSocketMessage(){
        try {
            Socket clientSocket = new Socket(host, port);
            OutputStream out = clientSocket.getOutputStream();
            out.write("Bonjour".getBytes());
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        showText("message sent...");
    }
}
