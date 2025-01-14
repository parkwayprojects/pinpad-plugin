package org.apache.cordova.posPlugin;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Trace;
import android.provider.Settings;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.dspread.xpos.CQPOSService;
import com.dspread.xpos.EmvAppTag;
import com.dspread.xpos.EmvCapkTag;
import com.dspread.xpos.QPOSService;
import com.dspread.xpos.QPOSService.CommunicationMode;
import com.dspread.xpos.QPOSService.Display;
import com.dspread.xpos.QPOSService.DoTradeResult;
import com.dspread.xpos.QPOSService.EMVDataOperation;
import com.dspread.xpos.QPOSService.EmvOption;
import com.dspread.xpos.QPOSService.Error;
import com.dspread.xpos.QPOSService.QPOSServiceListener;
import com.dspread.xpos.QPOSService.TransactionResult;
import com.dspread.xpos.QPOSService.TransactionType;
import com.dspread.xpos.QPOSService.UpdateInformationResult;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginManager;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class echoes a string called from JavaScript.
 */
public class dspread_pos_plugin extends CordovaPlugin {
	private MyPosListener listener;
	private QPOSService pos;
	private BluetoothAdapter mAdapter;
	private String sdkVersion;
	private String blueToothAddress;
	private List<BluetoothDevice> listDevice;
	private String terminalTime = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
	private String currencyCode = "156";
	private TransactionType transactionType = TransactionType.GOODS;
	ArrayList<String> list=new ArrayList<String>();
	private String amount = "";
	private String cashbackAmount = "";
	private List<Map<String, ?>> data = new ArrayList<Map<String, ?>>();
	private static final int PROGRESS_UP = 1001;
	private Hashtable<String, String> pairedDevice;
	private Activity activity;
	private CordovaWebView webView;
	private LocationManager lm;//【位置管理】
	private boolean posFlag=false;
	private List blueToothNameArr = new ArrayList();
	private Map map = new HashMap();
	private PluginResult pluginResult = null;

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		return super.execute(action, args, callbackContext);
	}

	@Override
	public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
		map.put(action,callbackContext.getCallbackId());
		if(action.equals("scanQPos2Mode")) {
			open(CommunicationMode.BLUETOOTH);//initial the open mode
			boolean a=pos.scanQPos2Mode(activity, 10);
			Toast.makeText(cordova.getActivity(), "scan success "+a, Toast.LENGTH_LONG).show();
		}else if(action.equals("setAmount")){
					TransactionType transactionType = TransactionType.GOODS;
					String amount = args.getString(0);
					pos.setPosDisplayAmountFlag(true);
					pos.setAmount(amount, "", "566", transactionType);
		}else if(action.equals("connectBluetoothDevice")){//connect
			pos.stopScanQPos2Mode();
			boolean isAutoConnect=args.getBoolean(0);
			String address=args.getString(1);
			int a = address.indexOf(" ");
			address = address.substring(a+1);
			TRACE.d("address==="+address);
			pos.connectBluetoothDevice(isAutoConnect, 20, address);
		}else if(action.equals("doTrade")){//start to do a trade
			TRACE.d("native--> doTrade");
			pos.doTrade(20);
		}else if(action.equals("getDeviceList")){//get all scaned devices
			TRACE.w("getDeviceList===");
			posFlag=true;
			listDevice=pos.getDeviceList();//can get all scaned device
			if(listDevice.size() > 0) {
				String[] macAddress = new String[listDevice.size()];
				String devices = "";
				for (int i = 0; i < listDevice.size(); i++) {
					macAddress[i] = listDevice.get(i).getName() + "(" + listDevice.get(i).getAddress() + "),";
					devices += macAddress[i];
				}
				TRACE.w("get devi==" + devices);
			}
		}else if(action.equals("stopScanQPos2Mode")){//stop scan bluetooth
			pos.stopScanQPos2Mode();
		}else if(action.equals("disconnectBT")){//discooect bluetooth
			pos.disconnectBT();
		}else if(action.equals("getQposInfo")){//get the pos info
			pos.getQposInfo();
		}else if(action.equals("getQposId")){//get the pos id
			pos.getQposId(20);
		}else if(action.equals("updateIPEK")){//update the ipek key
			String ipekGroup=args.getString(0);
			String trackksn=args.getString(1);
			String trackipek=args.getString(2);
			String trackipekCheckvalue=args.getString(3);
			String emvksn=args.getString(4);
			String emvipek=args.getString(5);
			String emvipekCheckvalue=args.getString(6);
			String pinksn=args.getString(7);
			String pinipek=args.getString(8);
			String pinipekCheckvalue=args.getString(9);
//        	pos.doUpdateIPEKOperation("00", "09117081600001E00001", "413DF85BD9D9A7C34EDDB2D2B5CA0C0F", "6A52E41A7F91C9F5", "09117081600001E00001", "413DF85BD9D9A7C34EDDB2D2B5CA0C0F", "6A52E41A7F91C9F5", "09117081600001E00001", "413DF85BD9D9A7C34EDDB2D2B5CA0C0F", "6A52E41A7F91C9F5");
			pos.doUpdateIPEKOperation(ipekGroup, trackksn, trackipek, trackipekCheckvalue, emvksn, emvipek, emvipekCheckvalue, pinksn, pinipek, pinipekCheckvalue);
		}else if(action.equals("updateEmvApp")){//update the emv app config
			list.add(EmvAppTag.Terminal_Default_Transaction_Qualifiers+"36C04000");
			list.add(EmvAppTag.Contactless_CVM_Required_limit+"000000060000");
			list.add(EmvAppTag.terminal_contactless_transaction_limit+"000000060000");
			pos.updateEmvAPP(EMVDataOperation.update,list);
		}else if(action.equals("updateEmvAPPByTlv")){
				pos.updateEmvAPPByTlv(EMVDataOperation.update, "9F1A020566");
		}else if(action.equals("updateEmvCAPK")){//update the emv capk config
			list.add(EmvCapkTag.RID+"A000000004");
			list.add(EmvCapkTag.Public_Key_Index+"F1");
			list.add(EmvCapkTag.Public_Key_Module+"A0DCF4BDE19C3546B4B6F0414D174DDE294AABBB828C5A834D73AAE27C99B0B053A90278007239B6459FF0BBCD7B4B9C6C50AC02CE91368DA1BD21AAEADBC65347337D89B68F5C99A09D05BE02DD1F8C5BA20E2F13FB2A27C41D3F85CAD5CF6668E75851EC66EDBF98851FD4E42C44C1D59F5984703B27D5B9F21B8FA0D93279FBBF69E090642909C9EA27F898959541AA6757F5F624104F6E1D3A9532F2A6E51515AEAD1B43B3D7835088A2FAFA7BE7");
			list.add(EmvCapkTag.Public_Key_CheckValue+"D8E68DA167AB5A85D8C3D55ECB9B0517A1A5B4BB");
			list.add(EmvCapkTag.Pk_exponent+"03");
			pos.updateEmvCAPK(EMVDataOperation.update, list);
		}else if(action.equals("setMasterKey")){//set the masterkey
			String key=args.getString(0);
			String checkValue=args.getString(1);
			pos.setMasterKey(key,checkValue);
		}else if(action.equals("updatePosFirmware")){//update pos firmware
			byte[] data=readLine("upgrader.asc");//upgrader.asc place in the assets folder
			pos.updatePosFirmware(data, blueToothAddress);//deviceAddress is BluetoothDevice address
			UpdateThread updateThread = new UpdateThread();
			updateThread.start();
		}else if(action.equals("updateEMVConfigByXml")){
			TRACE.d("native--> updateEMVConfigByXml");
			byte[] bytes = readAssetsLine("emv_profile_tlv.xml", cordova.getActivity());
			TRACE.d("bytes: "+ QPOSUtil.byteArray2Hex(bytes));
			pos.updateEMVConfigByXml(new String(bytes));
		}else if(action.equals("getIccCardNo")){
			TRACE.d("native--> getIccCardNo");
			pos.getIccCardNo(terminalTime);
		}
		return true;
	}

	public void callbackKeepResult(PluginResult.Status status,Boolean isKeep,String key, String message){
		pluginResult = new PluginResult(status,message);
		pluginResult.setKeepCallback(isKeep);
		CallbackContext callbackContext = new CallbackContext((String) map.get(key),webView);
		callbackContext.sendPluginResult(pluginResult);
	}

	public static byte[] readAssetsLine(String fileName, Context context) {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try {
			ContextWrapper contextWrapper = new ContextWrapper(context);
			AssetManager assetManager = contextWrapper.getAssets();
			InputStream inputStream = assetManager.open(fileName);
			byte[] data = new byte[512];
			int current = 0;
			while ((current = inputStream.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, current);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return  null;
		}
		return buffer.toByteArray();
	}

	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		// TODO Auto-generated method stub
		super.initialize(cordova, webView);
		this.activity=cordova.getActivity();
		this.webView=webView;
		requestPer();
	}

	//initial the pos
	private void open(CommunicationMode mode) {
		TRACE.d("open");
		listener = new MyPosListener();
		pos = QPOSService.getInstance(mode);
		if (pos == null) {
			TRACE.d("CommunicationMode unknow");
			return;
		}
		pos.setConext(cordova.getActivity());
		Handler handler = new Handler(Looper.myLooper());
		pos.initListener(handler, listener);
		sdkVersion = pos.getSdkVersion();
		TRACE.i("sdkVersion:"+sdkVersion);
		mAdapter=BluetoothAdapter.getDefaultAdapter();
	}

	private void requestPer(){
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		if (adapter != null && !adapter.isEnabled()) {//表示蓝牙不可用
			Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			activity.startActivity(enabler);
		}
		lm = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
		boolean ok = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if (ok) {//开了定位服务
			if (!cordova.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
				Log.e("POS_SDK", "没有权限");
				// 没有权限，申请权限。
				// 申请授权。
				cordova.requestPermission(this,100,Manifest.permission.ACCESS_COARSE_LOCATION);
			} else {
				// 有权限了，去放肆吧。
				Toast.makeText(activity, "Has permission!", Toast.LENGTH_SHORT).show();
			}
		} else {
			Log.e("BRG", "系统检测到未开启GPS定位服务");
			Toast.makeText(activity, "系统检测到未开启GPS定位服务", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent();
			intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			activity.startActivity(intent);
		}
	}

	@Override
	public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
		super.onRequestPermissionResult(requestCode, permissions, grantResults);
		switch (requestCode) {
			case 100: {
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// 权限被用户同意。
					Toast.makeText(activity, "Has open the permission!", Toast.LENGTH_LONG).show();
				} else {
					// 权限被用户拒绝了。
					Toast.makeText(activity, "Permission has been limited", Toast.LENGTH_LONG).show();
				}

			}
			break;
		}
	}

	private void sendMsg(int what) {
		Message msg = new Message();
		msg.what = what;
		mHandler.sendMessage(msg);
	}

	private Handler mHandler=new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 8003:
					Hashtable<String, String> h =  pos.getNFCBatchData();
					TRACE.w("nfc batchdata: "+h);
					String content = "\nNFCbatchData: "+h.get("tlv");
					break;
				default:
					break;
			}
		}
	};

	//read the buffer
	private byte[] readLine(String Filename) {

		String str = "";
		ByteArrayOutputStream buffer = new ByteArrayOutputStream(0);
		try {
			ContextWrapper contextWrapper = new ContextWrapper(cordova.getActivity());
			AssetManager assetManager = contextWrapper.getAssets();
			InputStream inputStream = assetManager.open(Filename);
			// BufferedReader br = new BufferedReader(new
			// InputStreamReader(inputStream));
			// str = br.readLine();
			int b = inputStream.read();
			while (b != -1) {
				buffer.write((byte) b);
				b = inputStream.read();
			}
			TRACE.d("-----------------------");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return buffer.toByteArray();
	}

	class UpdateThread extends Thread {
		public void run() {

			while (true) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				int progress = pos.getUpdateProgress();
				if (progress < 100) {
					Message msg = updata_handler.obtainMessage();
					msg.what = PROGRESS_UP;
					msg.obj = progress;
					msg.sendToTarget();
					continue;
				}
				Message msg = updata_handler.obtainMessage();
				msg.what = PROGRESS_UP;
				msg.obj = "update success";
				msg.sendToTarget();
				break;
			}
		};
	}

	private Handler updata_handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case PROGRESS_UP://update the firmware
					TRACE.i(msg.obj.toString() + "%");
					break;
				case 101://the callback of the connect the printer success
					Toast.makeText(cordova.getActivity(), "connect the printer success", Toast.LENGTH_LONG).show();
					break;
				default:
					break;
			}
		}
	};

	//our sdk api callback(success or fail)
	class MyPosListener extends CQPOSService {

		@Override
		public void onDeviceFound(BluetoothDevice arg0) {
			if(arg0!=null){
				String address=arg0.getAddress();
				String name=arg0.getName();
				String mac = name+" "+address;
				if (!blueToothNameArr.contains(mac)){
					blueToothNameArr.add(mac);
					TRACE.i("scaned the device:\n"+name+"("+address+")");
					if (name !=null){
						callbackKeepResult(PluginResult.Status.OK,true,"scanQPos2Mode",mac);
					}
				}
			}
		}

		@Override
		public void onQposIdResult(Hashtable<String, String> arg0) {
			if(arg0!=null){
				String posId = arg0.get("posId") == null ? "" : arg0
						.get("posId");
				String csn = arg0.get("csn") == null ? "" : arg0
						.get("csn");
				String psamId=arg0.get("psamId") == null ? "" : arg0
						.get("psamId");

				String content = "";
				content += "posId" + posId + "\n";
				content += "csn: " + csn + "\n";
				content += "conn: " + pos.getBluetoothState() + "\n";
				content += "psamId: " + psamId + "\n";
			}
		}

		@Override
		public void onRequestWaitingUser() {
			TRACE.d("onRequestWaitingUser()");
			callbackKeepResult(PluginResult.Status.OK,true,"doTrade","onRequestWaitingUser");
		}


		@Override
		public void onQposInfoResult(Hashtable<String, String> arg0) {
			TRACE.d("onQposInfoResult"+arg0);
			String isSupportedTrack1 = arg0.get("isSupportedTrack1") == null ? "" : arg0.get("isSupportedTrack1");
			String isSupportedTrack2 = arg0.get("isSupportedTrack2") == null ? "" : arg0.get("isSupportedTrack2");
			String isSupportedTrack3 = arg0.get("isSupportedTrack3") == null ? "" : arg0.get("isSupportedTrack3");
			String bootloaderVersion = arg0.get("bootloaderVersion") == null ? "" : arg0.get("bootloaderVersion");
			String firmwareVersion = arg0.get("firmwareVersion") == null ? "" : arg0.get("firmwareVersion");
			String isUsbConnected = arg0.get("isUsbConnected") == null ? "" : arg0.get("isUsbConnected");
			String isCharging = arg0.get("isCharging") == null ? "" : arg0.get("isCharging");
			String batteryLevel = arg0.get("batteryLevel") == null ? "" : arg0.get("batteryLevel");
			String batteryPercentage = arg0.get("batteryPercentage") == null ? ""
					: arg0.get("batteryPercentage");
			String hardwareVersion = arg0.get("hardwareVersion") == null ? "" : arg0.get("hardwareVersion");
			String SUB=arg0.get("SUB")== null ? "" : arg0.get("SUB");
			String content = "";
			content += "bootloader_version" + bootloaderVersion + "\n";
			content += "firmwareVersion" + firmwareVersion + "\n";
			content += "isUsbConnected" + isUsbConnected + "\n";
			content += "isCharging" + isCharging + "\n";
//			if (batteryPercentage==null || "".equals(batteryPercentage)) {
			content += "batteryLevel" + batteryLevel + "\n";
//			}else {
			content += "batteryPercentage"  + batteryPercentage + "\n";
//			}
			content += "hardwareVersion" + hardwareVersion + "\n";
			content += "SUB : " + SUB + "\n";
			content += "isSupportedTrack1" + isSupportedTrack1 + "\n";
			content += "isSupportedTrack2" + isSupportedTrack2 + "\n";
			content += "isSupportedTrack3" + isSupportedTrack3 + "\n";
		}


		@Override
		public void onDoTradeResult(DoTradeResult arg0, Hashtable<String, String> arg1) {
			if (arg0 == DoTradeResult.NONE) {
				TRACE.d("no_card_detected");
			} else if (arg0 == DoTradeResult.ICC) {
				TRACE.d("icc_card_inserted");
				TRACE.d("EMV ICC Start");
				pos.doEmvApp(EmvOption.START);//do the icc card trade
			} else if (arg0 == DoTradeResult.NOT_ICC) {
				TRACE.d("card_inserted(NOT_ICC)");
			} else if (arg0 == DoTradeResult.BAD_SWIPE) {
				TRACE.d("bad_swipe");
			} else if (arg0 == DoTradeResult.MCR) {//
				String content ="Swipe Card:\n";
				String formatID = arg1.get("formatID");
				if (formatID.equals("31") || formatID.equals("40") || formatID.equals("37") || formatID.equals("17") || formatID.equals("11") || formatID.equals("10")) {
					String maskedPAN = arg1.get("maskedPAN");
					String expiryDate = arg1.get("expiryDate");
					String cardHolderName = arg1.get("cardholderName");
					String serviceCode = arg1.get("serviceCode");
					String trackblock = arg1.get("trackblock");
					String psamId = arg1.get("psamId");
					String posId = arg1.get("posId");
					String pinblock = arg1.get("pinblock");
					String macblock = arg1.get("macblock");
					String activateCode = arg1.get("activateCode");
					String trackRandomNumber = arg1.get("trackRandomNumber");

					content += "format_id" + " " + formatID + "\n";
					content += "masked_pan" + " " + maskedPAN + "\n";
					content += "expiry_date" + " " + expiryDate + "\n";
					content += "cardholder_name" + " " + cardHolderName + "\n";

					content += "service_code"+ " " + serviceCode + "\n";
					content += "trackblock: " + trackblock + "\n";
					content += "psamId: " + psamId + "\n";
					content += "posId: " + posId + "\n";
					content += "pinBlock" + " " + pinblock + "\n";
					content += "macblock: " + macblock + "\n";
					content += "activateCode: " + activateCode + "\n";
					content += "trackRandomNumber: " + trackRandomNumber + "\n";
				} else if (formatID.equals("FF")) {
					String type = arg1.get("type");
					String encTrack1 = arg1.get("encTrack1");
					String encTrack2 = arg1.get("encTrack2");
					String encTrack3 = arg1.get("encTrack3");
					content += "cardType:" + " " + type + "\n";
					content += "track_1:" + " " + encTrack1 + "\n";
					content += "track_2:" + " " + encTrack2 + "\n";
					content += "track_3:" + " " + encTrack3 + "\n";
				} else {
					String orderID=arg1.get("orderId");
					String maskedPAN = arg1.get("maskedPAN");
					String expiryDate = arg1.get("expiryDate");
					String cardHolderName = arg1.get("cardholderName");
//					String ksn = arg1.get("ksn");
					String serviceCode = arg1.get("serviceCode");
					String track1Length = arg1.get("track1Length");
					String track2Length = arg1.get("track2Length");
					String track3Length = arg1.get("track3Length");
					String encTracks = arg1.get("encTracks");
					String encTrack1 = arg1.get("encTrack1");
					String encTrack2 = arg1.get("encTrack2");
					String encTrack3 = arg1.get("encTrack3");
					String partialTrack = arg1.get("partialTrack");
					// TODO
					String pinKsn = arg1.get("pinKsn");
					String trackksn = arg1.get("trackksn");
					String pinBlock = arg1.get("pinBlock");
					String encPAN = arg1.get("encPAN");
					String trackRandomNumber = arg1.get("trackRandomNumber");
					String pinRandomNumber = arg1.get("pinRandomNumber");
					if(orderID!=null&&!"".equals(orderID)){
						content+="orderID:"+orderID;
					}
					content += "formatID: " + formatID + "\n";
					content += "maskedPAN: " + maskedPAN + "\n";
					content += "expiryDate: " + expiryDate + "\n";
					content += "cardHolderName: " + cardHolderName + "\n";
//					content += getString(R.string.ksn) + " " + ksn + ",";
					content += "pinKsn: " + pinKsn + "\n";
					content += "trackksn: " + trackksn + "\n";
					content += "serviceCode: " + serviceCode + "\n";
					content += "track1Length: " + track1Length + "\n";
					content += "track2Length: " + track2Length + "\n";
					content += "track3Length: " + track3Length + "\n";
					content += "encTracks: " + encTracks + "\n";
					content += "encTrack1: " + encTrack1 + "\n";
					content += "encTrack2: " + encTrack2 + "\n";
					content += "encTrack3: " + encTrack3 + "\n";
					content += "partialTrack: " + partialTrack + "\n";
					content += "pinBlock: " + pinBlock + "\n";
					content += "encPAN: " + encPAN + "\n";
					content += "trackRandomNumber: " + trackRandomNumber + "\n";
					content += "pinRandomNumber: " + " " + pinRandomNumber;
					callbackKeepResult(PluginResult.Status.OK,true,"doTrade",content);
				}
				TRACE.d("=====:" + content);
			} else if ((arg0 == DoTradeResult.NFC_ONLINE) || (arg0 == DoTradeResult.NFC_OFFLINE)) {
				TRACE.d(arg0+", arg1: " + arg1);
//				nfcLog=arg1.get("nfcLog");
				String content = "Tap Card:"+"\n";
				String formatID = arg1.get("formatID");
				if (formatID.equals("31") || formatID.equals("40")
						|| formatID.equals("37") || formatID.equals("17")
						|| formatID.equals("11") || formatID.equals("10")) {
					String maskedPAN = arg1.get("maskedPAN");
					String expiryDate = arg1.get("expiryDate");
					String cardHolderName = arg1.get("cardholderName");
					String serviceCode = arg1.get("serviceCode");
					String trackblock = arg1.get("trackblock");
					String psamId = arg1.get("psamId");
					String posId = arg1.get("posId");
					String pinblock = arg1.get("pinblock");
					String macblock = arg1.get("macblock");
					String activateCode = arg1.get("activateCode");
					String trackRandomNumber = arg1
							.get("trackRandomNumber");

					content += "formatID" + " " + formatID
							+ "\n";
					content += "maskedPAN" + " " + maskedPAN
							+ "\n";
					content += "expiryDate" + " "
							+ expiryDate + "\n";
					content += "cardHolderName" + " "
							+ cardHolderName + "\n";

					content += "serviceCode" + " "
							+ serviceCode + "\n";
					content += "trackblock: " + trackblock + "\n";
					content += "psamId: " + psamId + "\n";
					content += "posId: " + posId + "\n";
					content += "pinblock" + " " + pinblock
							+ "\n";
					content += "macblock: " + macblock + "\n";
					content += "activateCode: " + activateCode + "\n";
					content += "trackRandomNumber: " + trackRandomNumber + "\n";
				} else {

					String maskedPAN = arg1.get("maskedPAN");
					String expiryDate = arg1.get("expiryDate");
					String cardHolderName = arg1.get("cardholderName");
//					String ksn = arg1.get("ksn");
					String serviceCode = arg1.get("serviceCode");
					String track1Length = arg1.get("track1Length");
					String track2Length = arg1.get("track2Length");
					String track3Length = arg1.get("track3Length");
					String encTracks = arg1.get("encTracks");
					String encTrack1 = arg1.get("encTrack1");
					String encTrack2 = arg1.get("encTrack2");
					String encTrack3 = arg1.get("encTrack3");
					String partialTrack = arg1.get("partialTrack");
					// TODO
					String pinKsn = arg1.get("pinKsn");
					String trackksn = arg1.get("trackksn");
					String pinBlock = arg1.get("pinBlock");
					String encPAN = arg1.get("encPAN");
					String trackRandomNumber = arg1
							.get("trackRandomNumber");
					String pinRandomNumber = arg1.get("pinRandomNumber");

					content +="formatID: " + formatID + "\n";
					content += "maskedPAN: " + maskedPAN + "\n";
					content += "expiryDate: " + expiryDate + "\n";
					content += "cardHolderName: " + cardHolderName + "\n";
					content += "pinKsn: " + pinKsn + "\n";
					content += "trackksn: " + trackksn + "\n";
					content += "trackksn: " + serviceCode + "\n";

					content += "track1Length: " + track1Length + "\n";

					content += "track2Length: " + track2Length + "\n";

					content += "track3Length: " + track3Length + "\n";

					content += "encTracks: " + encTracks + "\n";

					content += "encTracks1: " + encTrack1 + "\n";

					content += "encTracks2: " + encTrack2 + "\n";

					content += "encTracks3: " + encTrack3 + "\n";

					content += "partialTrack: " + partialTrack + "\n";

					content += "pinBlock: " + pinBlock + "\n";

					content += "encPAN: " + encPAN + "\n";
					content += "trackRandomNumber: " + trackRandomNumber + "\n";
					content += "pinRandomNumber: " + pinRandomNumber + "\n";
				}
				TRACE.w(arg0+": "+content);
//				sendMsg(8003);
				Hashtable<String, String> h =  pos.getNFCBatchData();
				TRACE.w("nfc batchdata: "+h);
				content += "NFCbatchData: "+h.get("tlv");
				callbackKeepResult(PluginResult.Status.OK,true,"doTrade",content);
			} else if ((arg0 == DoTradeResult.NFC_DECLINED) ) {
				TRACE.d("transaction_declined");
			}else if (arg0 == DoTradeResult.NO_RESPONSE) {
				TRACE.d("card_no_response");
			}
		}

		@Override
		public void onRequestDeviceScanFinished() {
			TRACE.i("scan finished");
			Toast.makeText(activity,"scan finished",Toast.LENGTH_LONG).show();
		}

		@Override
		public void onRequestDisplay(Display arg0) {
			TRACE.d("onRequestDisplay");

			String msg = "";
			if (arg0 == Display.CLEAR_DISPLAY_MSG) {
				msg = "" ;
			} else if(arg0 == Display.MSR_DATA_READY){
				AlertDialog.Builder builder=new AlertDialog.Builder(cordova.getActivity());
				builder.setTitle("???");
				builder.setMessage("Success,Contine ready");
				builder.setPositiveButton("???", null);
				builder.show();
			}else if (arg0 == Display.PLEASE_WAIT) {
				msg = "please wait..";
			} else if (arg0 == Display.REMOVE_CARD) {
				msg = "remove card";
			} else if (arg0 == Display.TRY_ANOTHER_INTERFACE) {
				msg = "try another interface";
			} else if (arg0 == Display.PROCESSING) {
				msg = "processing...";
			} else if (arg0 == Display.INPUT_PIN_ING) {
				msg = "please input pin on pos";
			} else if (arg0 == Display.MAG_TO_ICC_TRADE) {
				msg = "please insert chip card on pos";
			}else if (arg0 == Display.CARD_REMOVED) {
				msg = "card removed";
			}
			TRACE.d(msg);
			callbackKeepResult(PluginResult.Status.OK,true,"doTrade",msg);
		}

		@Override
		public void onRequestIsServerConnected() {
			TRACE.d("onRequestIsServerConnected");
			pos.isServerConnected(true);
		}

		@Override
		public void onRequestNoQposDetected() {
			TRACE.w("onRequestNoQposDetected");
			Toast.makeText(cordova.getActivity(), "onRequestNoQposDetected", Toast.LENGTH_LONG).show();
			//callbackKeepResult(PluginResult.Status.OK,true,"connectBluetoothDevice","onRequestNoQposDetected");
		}

		@Override
		public void onRequestNoQposDetectedUnbond() {

		}

		@Override
		public void onRequestOnlineProcess(String arg0) {
			TRACE.d("onRequestOnlineProcess");
			TRACE.i("return transaction online data:"+arg0);
			Hashtable<String, String> decodeData = pos.anlysEmvIccData(arg0);
			TRACE.i("decodeData:" + decodeData);
			//go online
			callbackKeepResult(PluginResult.Status.OK,true,"doTrade","onRequestOnlineProcess: " + arg0);
			pos.sendOnlineProcessResult("8A023030");
		}

		@Override
		public void onRequestQposConnected() {
			TRACE.w("onRequestQposConnected");
			Toast.makeText(cordova.getActivity(), "onRequestQposConnected", Toast.LENGTH_LONG).show();
			callbackKeepResult(PluginResult.Status.OK,true,"connectBluetoothDevice","onRequestQposConnected");
		}

		@Override
		public void onRequestQposDisconnected() {
			TRACE.w("onRequestQposDisconnected");
			Toast.makeText(cordova.getActivity(), "onRequestQposDisconnected", Toast.LENGTH_LONG).show();
			callbackKeepResult(PluginResult.Status.OK,true,"connectBluetoothDevice","onRequestQposDisconnected");
		}

		@Override
		public void onRequestSelectEmvApp(ArrayList<String> arg0) {
			TRACE.d("onRequestSelectEmvApp");
			TRACE.d("pls choose App -- S??emv card config");
			String[] appNameList = new String[arg0.size()];
			for (int i = 0; i < appNameList.length; ++i) {
				TRACE.d("i=" + i + "," + arg0.get(i));
				appNameList[i] = arg0.get(i);
			}
			pos.selectEmvApp(0);//choose one emv card
//			pos.cancelSelectEmvApp();//cancel select the emv card config
		}

		@Override
		public void onRequestSetAmount() {
			TRACE.d("onRequestSetAmount");
			callbackKeepResult(PluginResult.Status.OK,true,"doTrade","onRequestSetAmount");
		}

		@Override
		public void onRequestSetPin() {
			TRACE.d("onRequestSetPin");
			String pin="";
			if (pin.length() >= 4 && pin.length() <= 12) {
				pos.sendPin(pin);
			}
		}

		@Override
		public void onRequestTime() {
			TRACE.d("onRequestTime");
			pos.sendTime(terminalTime);
			TRACE.d("request_terminal_time:" + " " + terminalTime);
		}

		@Override
		public void onRequestBatchData(String arg0) {
			if(arg0!=null){
				callbackKeepResult(PluginResult.Status.OK,true,"doTrade","onRequestBatchData: " + arg0);
			}else{
				callbackKeepResult(PluginResult.Status.ERROR,true,"doTrade","");
			}
		}

		@Override
		public void onRequestTransactionResult(TransactionResult arg0) {
			TRACE.d("onRequestTransactionResult");
			String message = "";
			if (arg0 == TransactionResult.APPROVED) {
				TRACE.d("TransactionResult.APPROVED");
				message = "transaction_approved" + "\n" + "amount" + ": $" + amount + "\n";
				if (!cashbackAmount.equals("")) {
					message += "cashbackAmount" + ": INR" + cashbackAmount;
				}
			} else if (arg0 == TransactionResult.TERMINATED) {
				message = "TERMINATED";
				TRACE.d("TERMINATED");
			} else if (arg0 == TransactionResult.DECLINED) {
				message = "DECLINED";
				TRACE.d("DECLINED");
			} else if (arg0 == TransactionResult.CANCEL) {
				message = "CANCEL";
				TRACE.d("CANCEL");
			} else if (arg0 == TransactionResult.CAPK_FAIL) {
				message = "CAPK_FAIL";
				TRACE.d("CAPK_FAIL");
			} else if (arg0 == TransactionResult.NOT_ICC) {
				message = "NOT_ICC";
				TRACE.d("NOT_ICC");
			} else if (arg0 == TransactionResult.SELECT_APP_FAIL) {
				message = "SELECT_APP_FAIL";
				TRACE.d("SELECT_APP_FAIL");
			} else if (arg0 == TransactionResult.DEVICE_ERROR) {
				message = "DEVICE_ERROR";
				TRACE.d("DEVICE_ERROR");
			} else if(arg0 == TransactionResult.TRADE_LOG_FULL){
				TRACE.d("pls clear the trace log and then to begin do trade");
			}else if (arg0 == TransactionResult.CARD_NOT_SUPPORTED) {
				message = "CARD_NOT_SUPPORTED";
				TRACE.d("CARD_NOT_SUPPORTED");
			} else if (arg0 == TransactionResult.MISSING_MANDATORY_DATA) {
				TRACE.d("MISSING_MANDATORY_DATA");
				message = "MISSING_MANDATORY_DATA";
			} else if (arg0 == TransactionResult.CARD_BLOCKED_OR_NO_EMV_APPS) {
				message = "CARD_BLOCKED_OR_NO_EMV_APPS";
				TRACE.d("CARD_BLOCKED_OR_NO_EMV_APPS");
			} else if (arg0 == TransactionResult.INVALID_ICC_DATA) {
				message = "INVALID_ICC_DATA";
				TRACE.d("INVALID_ICC_DATA");
			}else if (arg0 == TransactionResult.FALLBACK) {
				message = "FALLBACK";
				TRACE.d("FALLBACK");
			}else if (arg0 == TransactionResult.NFC_TERMINATED) {
				message = "NFC_TERMINATED";
				TRACE.d("NFC_TERMINATED");
			} else if (arg0 == TransactionResult.CARD_REMOVED) {
				message = "CARD_REMOVED";
				TRACE.d("CARD_REMOVED");
			}
			callbackKeepResult(PluginResult.Status.OK,true,"doTrade",message);
		}


		@Override
		public void onRequestSignatureResult(byte[] arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onEmvICCExceptionData(String arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onGetCardNoResult(String arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onGetPosComm(int arg0, String arg1, String arg2) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onGetShutDownTime(String arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onGetSleepModeTime(String arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onLcdShowCustomDisplay(boolean arg0) {
			// TODO Auto-generated method stub

		}


		@Override
		public void onPinKey_TDES_Result(String arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onQposDoGetTradeLog(String arg0, String arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onRequestDevice() {

		}

		@Override
		public void onGetKeyCheckValue(List<String> list) {

		}

		@Override
		public void onGetDevicePubKey(String s) {

		}


		@Override
		public void onTradeCancelled() {

		}

		@Override
		public void onQposIsCardExistInOnlineProcess(boolean b) {

		}

		@Override
		public void onReturnSetConnectedShutDownTimeResult(boolean b) {

		}

		@Override
		public void onReturnGetConnectedShutDownTimeResult(String s) {

		}

		@Override
		public void onQposDoGetTradeLogNum(String arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onQposDoSetRsaPublicKey(boolean arg0) {
			// TODO Auto-generated method stub

		}


		@Override
		public void onQposCertificateInfoResult(List<String> list) {

		}

		@Override
		public void onQposIsCardExist(boolean arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onQposKsnResult(Hashtable<String, String> arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onRequestUpdateKey(String arg0) {

		}

		@Override
		public void onRequestFinalConfirm() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onRequestUpdateWorkKeyResult(UpdateInformationResult arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onRequestSendTR31KeyResult(boolean b) {

		}

		@Override
		public void onQposRequestPinResult(List<String> list, int i) {

		}

		@Override
		public void onReturnD20SleepTimeResult(boolean b) {

		}

		@Override
		public void onQposRequestPinStartResult(List<String> list) {

		}

		@Override
		public void onQposPinMapSyncResult(boolean b, boolean b1) {

		}

		@Override
		public void onReturnRsaResult(String s) {

		}

		@Override
		public void onQposInitModeResult(boolean b) {

		}

		@Override
		public void onD20StatusResult(String s) {

		}

		@Override
		public void onQposTestSelfCommandResult(boolean b, String s) {

		}

		@Override
		public void onQposTestCommandResult(boolean b, String s) {

		}

		@Override
		public void onReturnApduResult(boolean arg0, String arg1, int arg2) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onReturnBatchSendAPDUResult(LinkedHashMap<Integer, String> arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onReturnCustomConfigResult(boolean arg0, String arg1) {
			// TODO Auto-generated method stub
			if (arg0){
			}
		}

		@Override
		public void onRetuenGetTR31Token(String s) {

		}

		@Override
		public void onReturnDownloadRsaPublicKey(HashMap<String, String> arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onReturnGetEMVListResult(String arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onReturnGetCustomEMVListResult(Map<String, String> map) {

		}

		@Override
		public void onReturnGetPinResult(Hashtable<String, String> arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onReturnGetQuickEmvResult(boolean arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onReturnNFCApduResult(boolean arg0, String arg1, int arg2) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onReturnPowerOffIccResult(boolean arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onReturnPowerOffNFCResult(boolean arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onReturnPowerOnIccResult(boolean arg0, String arg1, String arg2, int arg3) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onReturnPowerOnNFCResult(boolean arg0, String arg1, String arg2, int arg3) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onReturnReversalData(String arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onReturnGetPinInputResult(int i) {

		}

		@Override
		public void onReturnGetKeyBoardInputResult(String s) {

		}

		@Override
		public void onReturnSetMasterKeyResult(boolean arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onReturnSetSleepTimeResult(boolean arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onReturnUpdateEMVRIDResult(boolean arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onReturnUpdateEMVResult(boolean arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onReturnUpdateIPEKResult(boolean arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onReturnRSAResult(String s) {

		}

		@Override
		public void onReturniccCashBack(Hashtable<String, String> arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSearchMifareCardResult(Hashtable<String, String> arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onBatchReadMifareCardResult(String s, Hashtable<String, List<String>> hashtable) {

		}

		@Override
		public void onBatchWriteMifareCardResult(String s, Hashtable<String, List<String>> hashtable) {

		}

		@Override
		public void onSetBuzzerResult(boolean arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSetBuzzerTimeResult(boolean b) {

		}

		@Override
		public void onSetBuzzerStatusResult(boolean b) {

		}

		@Override
		public void onGetBuzzerStatusResult(String s) {

		}

		@Override
		public void onSetManagementKey(boolean arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSetParamsResult(boolean arg0, Hashtable<String, Object> arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSetVendorIDResult(boolean b, Hashtable<String, Object> hashtable) {

		}

		@Override
		public void onSetSleepModeTime(boolean arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onUpdateMasterKeyResult(boolean arg0, Hashtable<String, String> arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onUpdatePosFirmwareResult(UpdateInformationResult arg0) {
			if(arg0==null){
				return;
			}else if(arg0==UpdateInformationResult.UPDATE_FAIL){
				TRACE.d("update fail");
			}else if(arg0==UpdateInformationResult.UPDATE_SUCCESS){
				TRACE.d("update success");
			}else if(arg0==UpdateInformationResult.UPDATE_PACKET_VEFIRY_ERROR){
				TRACE.d("update packet error");
			}
		}

		@Override
		public void onVerifyMifareCardResult(boolean arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onWaitingforData(String arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onWriteBusinessCardResult(boolean arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onWriteMifareCardResult(boolean arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void transferMifareData(String arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void verifyMifareULData(Hashtable<String, String> arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void writeMifareULData(String arg0) {
			// TODO Auto-generated method stub

		}

	}
}
