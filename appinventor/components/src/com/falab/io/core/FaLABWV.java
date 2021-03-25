package com.falab.io.core;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.HVArrangement;
import com.google.appinventor.components.runtime.PermissionResultHandler;
import com.google.appinventor.components.runtime.util.JsonUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.net.http.SslCertificate;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.HttpAuthHandler;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

@DesignerComponent(version = 1, versionName = "1", description = "An extended form of Web Viewer <br> Developed by Sunny Gupta", category = ComponentCategory.USERINTERFACE, nonVisible = true, iconName = "aiwebres/world-wide-web.png", helpUrl = "https://github.com/vknow360/CustomWebView", androidMinSdk = 21)
//@UsesActivities(activities = {@ActivityElement(intentFilters = {@IntentFilterElement(actionElements = {@ActionElement(name = "android.intent.action.VIEW")}, categoryElements = {@CategoryElement(name = "android.intent.category.DEFAULT"), @CategoryElement(name = "android.intent.category.BROWSABLE")}, dataElements = {@DataElement(scheme = "http"), @DataElement(scheme = "https")}), @IntentFilterElement(actionElements = {@ActionElement(name = "android.intent.action.VIEW")}, categoryElements = {@CategoryElement(name = "android.intent.category.DEFAULT"), @CategoryElement(name = "android.intent.category.BROWSABLE")}, dataElements = {@DataElement(scheme = "http"), @DataElement(scheme = "https"), @DataElement(mimeType = "text/html"), @DataElement(mimeType = "text/plain"), @DataElement(mimeType = "application/xhtml+xml")})},name="appinventor.ai_vknow360.CustomWebView.Screen1",launchMode = "singleTask")})
@SimpleObject(external = true)
@UsesPermissions(permissionNames = "android.permission.WRITE_EXTERNAL_STORAGE,android.permission.ACCESS_DOWNLOAD_MANAGER,android.permission.ACCESS_FINE_LOCATION,android.permission.RECORD_AUDIO, android.permission.MODIFY_AUDIO_SETTINGS, android.permission.CAMERA,android.permission.VIBRATE,android.webkit.resource.VIDEO_CAPTURE,android.webkit.resource.AUDIO_CAPTURE,android.launcher.permission.INSTALL_SHORTCUT")
public final class FaLABWV extends AndroidNonvisibleComponent {
	public class ChromeClient extends WebChromeClient {
		private View mCustomView;
		private WebChromeClient.CustomViewCallback mCustomViewCallback;
		private int mOriginalOrientation;
		private int mOriginalSystemUiVisibility;
		private final int FULL_SCREEN_SETTING = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE;

		@Override
		public void onCloseWindow(WebView window) {
			OnCloseWindowRequest();
		}

		@Override
		public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
			OnConsoleMessage(consoleMessage.message(), consoleMessage.lineNumber(), consoleMessage.lineNumber(),
					consoleMessage.messageLevel().toString());
			return true;
		}

		@Override
		public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
			// Dialog Create Code
			WebView newWebView = new WebView(activity);
			WebSettings webSettings = newWebView.getSettings();
			webSettings.setJavaScriptEnabled(true);
			webSettings.setDomStorageEnabled(true);

			final Dialog dialog = new Dialog(activity);
			dialog.setContentView(newWebView);

			ViewGroup.LayoutParams params = dialog.getWindow().getAttributes();
			params.width = ViewGroup.LayoutParams.MATCH_PARENT;
			params.height = ViewGroup.LayoutParams.MATCH_PARENT;
			dialog.getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
			dialog.show();

			newWebView.setWebChromeClient(new WebChromeClient() {
				@Override
				public void onCloseWindow(WebView window) {
					dialog.dismiss();
				}
			});

			// WebView Popup에서 내용이 안보이고 빈 화면만 보여 아래 코드 추가
			newWebView.setWebViewClient(new WebViewClient() {
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
					return false;
				}
			});

			((WebView.WebViewTransport) resultMsg.obj).setWebView(newWebView);
			resultMsg.sendToTarget();
			return true;

//			if (SupportMultipleWindows()) {
//				resultObj = resultMsg;
//
//				WebView.HitTestResult result = view.getHitTestResult();
//				int type = result.getType();
//				String data = result.getExtra();
//				OnNewWindowRequest(resultObj.toString(), isDialog, isUserGesture);
//			}
//			return SupportMultipleWindows();
		}

		@Override
		public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
			final GeolocationPermissions.Callback theCallback = callback;
			final String theOrigin = origin;
			if (!prompt) {
				callback.invoke(origin, true, true);
				return;
			}
			{
				AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
				alertDialog.setCancelable(false);
				alertDialog.setTitle("Permission Request");
				if (origin.equals("file://")) {
					origin = "This Application";
				}
				alertDialog.setMessage(origin + " would like to access your location.");
				alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Allow", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						theCallback.invoke(theOrigin, true, true);
					}
				});
				alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Refuse", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						theCallback.invoke(theOrigin, false, true);
					}
				});
				alertDialog.show();
			}
		}

		@Override
		public void onHideCustomView() {
			OnHideCustomView();
			((FrameLayout) activity.getWindow().getDecorView()).removeView(mCustomView);
			mCustomView = null;
			activity.getWindow().getDecorView().setSystemUiVisibility(mOriginalSystemUiVisibility);
			activity.setRequestedOrientation(mOriginalOrientation);
			mCustomViewCallback.onCustomViewHidden();
			mCustomViewCallback = null;
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
		}

		@Override
		public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
			OnJsAlert(url, message);
			jsAlert = result;
			return EnableJS();
		}

		@Override
		public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
			jsResult = result;
			OnJsConfirm(url, message);
			return EnableJS();
		}

		@Override
		public boolean onJsPrompt(WebView view, String url, String message, String defaultValue,
				JsPromptResult result) {
			jsPromptResult = result;
			OnJsPrompt(url, message, defaultValue);
			return EnableJS();
		}

		@Override
		public void onPermissionRequest(PermissionRequest request) {
			if (!prompt) {
				request.grant(request.getResources());
			} else {
				permissionRequest = request;
				String[] strings = request.getResources();
				List<String> permissions = Arrays.asList(strings);
				OnPermissionRequest(permissions);
			}
		}

		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			OnProgressChanged(newProgress);
		}

		@Override
		public void onShowCustomView(View paramView, CustomViewCallback paramCustomViewCallback) {
			OnShowCustomView();
			if (mCustomView != null) {
				onHideCustomView();
				return;
			}
			mCustomView = paramView;
			mOriginalSystemUiVisibility = activity.getWindow().getDecorView().getSystemUiVisibility();
			mOriginalOrientation = activity.getRequestedOrientation();
			mCustomViewCallback = paramCustomViewCallback;
			((FrameLayout) activity.getWindow().getDecorView()).addView(mCustomView,
					new FrameLayout.LayoutParams(-1, -1));
			activity.getWindow().getDecorView().setSystemUiVisibility(FULL_SCREEN_SETTING);
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
			mCustomView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
				@Override
				public void onSystemUiVisibilityChange(int i) {
					updateControls();
				}
			});
		}

		@Override
		public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> filePathCallback,
				FileChooserParams fileChooserParams) {
			mFilePathCallback = filePathCallback;
			FileUploadNeeded(fileChooserParams.getAcceptTypes()[0], fileChooserParams.isCaptureEnabled());
			return FileAccess();
		}

		void updateControls() {
			FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mCustomView.getLayoutParams();
			params.bottomMargin = 0;
			params.topMargin = 0;
			params.leftMargin = 0;
			params.rightMargin = 0;
			params.height = -1;
			params.width = -1;
			mCustomView.setLayoutParams(params);
			activity.getWindow().getDecorView().setSystemUiVisibility(FULL_SCREEN_SETTING);
		}
	}

	public class PrintDocumentAdapterWrapper extends PrintDocumentAdapter {

		private final PrintDocumentAdapter delegate;

		public PrintDocumentAdapterWrapper(PrintDocumentAdapter adapter) {
			super();
			delegate = adapter;
		}

		@Override
		public void onFinish() {
			delegate.onFinish();
			GotPrintResult(jobName, printJob.isCompleted(), printJob.isFailed(), printJob.isBlocked());
		}

		@Override
		public void onLayout(PrintAttributes printAttributes, PrintAttributes printAttributes1,
				CancellationSignal cancellationSignal, LayoutResultCallback layoutResultCallback, Bundle bundle) {
			delegate.onLayout(printAttributes, printAttributes1, cancellationSignal, layoutResultCallback, bundle);
		}

		@Override
		public void onWrite(PageRange[] pageRanges, ParcelFileDescriptor parcelFileDescriptor,
				CancellationSignal cancellationSignal, WriteResultCallback writeResultCallback) {
			delegate.onWrite(pageRanges, parcelFileDescriptor, cancellationSignal, writeResultCallback);
		}
	}

	public class WebClient extends WebViewClient {
		public HashMap<String, Boolean> loadedUrls = new HashMap<>();

		@Override
		public void onFormResubmission(WebView view, Message dontResend, Message resend) {
			dontSend = dontResend;
			reSend = resend;
			OnFormResubmission();
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			if (isLoading) {
				isLoading = false;
				PageLoaded();
			}
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			if (!isLoading) {
				PageStarted(url);
				isLoading = true;
			}
		}

		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			OnErrorReceived(description, errorCode, failingUrl);
		}

		@Override
		public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
			OnErrorReceived(error.getDescription().toString(), error.getErrorCode(), request.getUrl().toString());
		}

		@Override
		public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
			httpAuthHandler = handler;
			OnReceivedHttpAuthRequest(host, realm);
		}

		@Override
		public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
			OnErrorReceived(errorResponse.getReasonPhrase(), errorResponse.getStatusCode(),
					request.getUrl().toString());
		}

		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
			if (ignoreSslErrors) {
				handler.proceed();
			} else {
				handler.cancel();
			}
		}

		@Override
		public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
			return null;
		}

		@Override
		public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
			return null;

		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (url.startsWith("http")) {
				return !followLinks;
			} else {
				if (deepLinks) {
					return DeepLinkParser(url);
				}
			}
			return false;
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
			String url1 = request.getUrl().toString();
			if (url1.startsWith("http")) {
				return !followLinks;
			} else {
				if (deepLinks) {
					return DeepLinkParser(url1);
				}
			}
			return false;
		}
	}

	public class WebViewInterface {
		String webViewString;

		WebViewInterface() {
			webViewString = "";
		}

		@JavascriptInterface
		public String getWebViewString() {
			return webViewString;
		}

		@JavascriptInterface
		public void setWebViewString(final String newString) {
			webViewString = newString;
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					WebViewStringChanged(newString);
				}
			});
		}

		public void setWebViewStringFromBlocks(final String newString) {
			webViewString = newString;
		}
	}

	public Activity activity;
	public WebView webView;
	public Context context;
	public boolean followLinks = true;
	public boolean prompt = true;
	public String UserAgent = "";
	public boolean ignoreSslErrors = false;
	WebViewInterface wvInterface;
	public JsPromptResult jsPromptResult;
	private String MOBILE_USER_AGENT = "";
	private ValueCallback<Uri[]> mFilePathCallback;
	public Message dontSend;
	public Message reSend;
	public boolean hasLocationAccess;
	public boolean hasWriteAccess;
	public PermissionRequest permissionRequest;
	public PrintJob printJob;
	public CookieManager cookieManager;
	public JsResult jsResult;
	public JsResult jsAlert;
	public HttpAuthHandler httpAuthHandler;
	public boolean deepLinks = false;
	public String jobName = "";
	public boolean isLoading = false;
	public boolean desktopMode = false;

	public int zoomPercent = 100;

	public boolean zoomEnabled = true;

	public boolean displayZoom = true;

	public Message resultObj;

	public float deviceDensity;

	private boolean isInit = false;

	public FaLABWV(ComponentContainer container) {
		super(container.$form());
		activity = container.$context();
		context = activity;
		wvInterface = new WebViewInterface();
		cookieManager = CookieManager.getInstance();
		deviceDensity = container.$form().deviceDensity();
		hasWriteAccess = context.checkCallingOrSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") == 0;
		hasLocationAccess = context.checkCallingOrSelfPermission("android.permission.ACCESS_FINE_LOCATION") == 0;
		webView = new WebView(context);
		resetWebView(webView);
	}

	@SimpleEvent(description = "Event raised after 'SaveArchive' method.If 'success' is true then returns file path else empty string.")
	public void AfterArchiveSaved(boolean success, String filePath) {
		EventDispatcher.dispatchEvent(this, "AfterArchiveSaved", success, filePath);
	}

	@SimpleEvent(description = "Event raised after evaluating Js and returns result.")
	public void AfterJavaScriptEvaluated(String result) {
		EventDispatcher.dispatchEvent(this, "AfterJavaScriptEvaluated", result);
	}

	@SimpleProperty(description = "Returnss whether the WebView should load image resources")
	public boolean AutoLoadImages() {
		return webView.getSettings().getLoadsImagesAutomatically();
	}

	@SimpleProperty(description = "Sets whether the WebView should load image resources")
	public void AutoLoadImages(boolean bool) {
		webView.getSettings().setBlockNetworkImage(!bool);
		webView.getSettings().setLoadsImagesAutomatically(bool);
	}

	@SimpleProperty(description = "Returns whether the WebView requires a user gesture to play media")
	public boolean AutoplayMedia() {
		return webView.getSettings().getMediaPlaybackRequiresUserGesture();
	}

	@SimpleProperty(description = "Sets whether the WebView requires a user gesture to play media")
	public void AutoplayMedia(boolean bool) {
		webView.getSettings().setMediaPlaybackRequiresUserGesture(bool);
	}

	@SimpleProperty(description = "Sets background color of webview")
	public void BackgroundColor(int bgColor) {
		webView.setBackgroundColor(bgColor);
	}

	@SimpleProperty(description = "Returns whether the WebView should not load resources from the network")
	public boolean BlockNetworkLoads() {
		return webView.getSettings().getBlockNetworkLoads();
	}

	@SimpleProperty(description = "Sets whether the WebView should not load resources from the network.Use this to save data.")
	public void BlockNetworkLoads(boolean block) {
		webView.getSettings().setBlockNetworkLoads(block);
	}

	@SimpleProperty(description = "Gets cache mode of active webview")
	public int CacheMode() {
		return webView.getSettings().getCacheMode();
	}

	@SimpleProperty(description = "Sets cache mode for active webview")
	public void CacheMode(int mode) {
		webView.getSettings().setCacheMode(mode);
	}

	public void CancelJsRequests() {
		if (jsAlert != null) {
			jsAlert.cancel();
			jsAlert = null;
		} else if (jsResult != null) {
			jsResult.cancel();
			jsResult = null;
		} else if (jsPromptResult != null) {
			jsPromptResult.cancel();
			jsPromptResult = null;
		} else if (mFilePathCallback != null) {
			mFilePathCallback.onReceiveValue(null);
			mFilePathCallback = null;
		}
	}

	@SimpleFunction(description = "Cancels current print job. You can request cancellation of a queued, started, blocked, or failed print job.")
	public void CancelPrinting() throws Exception {
		printJob.cancel();
	}

	@SimpleFunction(description = "Gets whether this WebView has a back history item")
	public boolean CanGoBack() {
		return webView.canGoBack();
	}

	@SimpleFunction(description = "Gets whether the page can go back or forward the given number of steps.")
	public boolean CanGoBackOrForward(int steps) {
		return webView.canGoBackOrForward(steps);
	}

	@SimpleFunction(description = "Gets whether this WebView has a forward history item.")
	public boolean CanGoForward() {
		return webView.canGoForward();
	}

	@SimpleFunction(description = "Clears the resource cache.")
	public void ClearCache() {
		webView.clearCache(true);
	}

	@SimpleFunction(description = "Removes all cookies and raises 'CookiesRemoved' event")
	public void ClearCookies() {
		cookieManager.removeAllCookies(new ValueCallback<Boolean>() {
			@Override
			public void onReceiveValue(Boolean aBoolean) {
				CookiesRemoved(aBoolean);
			}
		});
		cookieManager.flush();
	}

	@SimpleFunction(description = "Tells this WebView to clear its internal back/forward list.")
	public void ClearInternalHistory() {
		webView.clearHistory();
	}

	@SimpleFunction(description = "Clear all location preferences.")
	public void ClearLocation() {
		GeolocationPermissions.getInstance().clearAll();
	}

	@SimpleFunction(description = "Clears the highlighting surrounding text matches.")
	public void ClearMatches() {
		webView.clearMatches();
	}

	@SimpleFunction(description = "Whether to proceed JavaScript originated request")
	public void ConfirmJs(boolean confirm) {
		if (jsResult != null) {
			if (confirm) {
				jsResult.confirm();
			} else {
				jsResult.cancel();
			}
			jsResult = null;
		}
	}

	@SimpleFunction(description = "Gets height of HTML content")
	public int ContentHeight() {
		return d2p(webView.getContentHeight());
	}

	@SimpleFunction(description = "Inputs a confirmation response to Js")
	public void ContinueJs(String input) {
		if (jsPromptResult != null) {
			jsPromptResult.confirm(input);
			jsPromptResult = null;
		}
	}

	@SimpleEvent(description = "Event raised after 'ClearCokies' method with result")
	public void CookiesRemoved(boolean successful) {
		EventDispatcher.dispatchEvent(this, "CookiesRemoved", successful);
	}

	@SimpleFunction(description = "Creates a shortcut of given website on home screen")
	public void CreateShortcut(String url, String iconPath, String title) {
		try {
			Bitmap img = BitmapFactory.decodeFile(iconPath);
			if (img != null) {
				String screen = context.getSharedPreferences("TinyDB1", Context.MODE_PRIVATE).getString("ssn", "");
				String pkg = context.getPackageName();
				Intent intent = new Intent();
				String clsName = Objects.requireNonNull(context.getPackageManager().resolveActivity(
						context.getPackageManager().getLaunchIntentForPackage(pkg), 0)).activityInfo.name.replaceAll(
								"Screen1",
								screen.length() == 0 ? "Screen1" : JsonUtil.getObjectFromJson(screen, true).toString());
				intent.setClassName(context, clsName);
				List<String> startValue = new ArrayList<>();
				startValue.add(url);
				startValue.add("2");
				intent.putExtra("APP_INVENTOR_START", JsonUtil.getJsonRepresentation(startValue));
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
					Intent installer = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
					installer.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
					installer.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
					installer.putExtra(Intent.EXTRA_SHORTCUT_ICON, img);
					installer.putExtra("duplicate", false);
					context.sendBroadcast(installer);
				} else {
					ShortcutManager shortcutManager = (ShortcutManager) context
							.getSystemService(Context.SHORTCUT_SERVICE);
					if (shortcutManager.isRequestPinShortcutSupported()) {
						ShortcutInfo shortcutInfo = new ShortcutInfo.Builder(context, title).setShortLabel(title)
								.setIcon(Icon.createWithBitmap(img)).setIntent(intent).build();
						Intent pinnedShortcutCallbackIntent = shortcutManager.createShortcutResultIntent(shortcutInfo);
						PendingIntent successCallback = PendingIntent.getBroadcast(context, 0,
								pinnedShortcutCallbackIntent, 0);
						shortcutManager.requestPinShortcut(shortcutInfo, successCallback.getIntentSender());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SimpleProperty(description = "Title of the page currently viewed", category = PropertyCategory.BEHAVIOR)
	public String CurrentPageTitle() {
		return (webView.getTitle() == null) ? "" : webView.getTitle();
	}

	@SimpleProperty(description = "URL of the page currently viewed")
	public String CurrentUrl() {
		return (webView.getUrl() == null) ? "" : webView.getUrl();
	}

	public int d2p(int d) {
		return Math.round(d / deviceDensity);
	}

	public boolean DeepLinkParser(String url) {
		PackageManager packageManager = context.getPackageManager();
		Intent intent;
		if (url.startsWith("tel:")) {
			intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
			activity.startActivity(intent);
			return true;
		} else if (url.startsWith("mailto:") || url.startsWith("sms:")) {
			intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			activity.startActivity(intent);
			return true;
		} else if (url.startsWith("whatsapp:")) {
			intent = new Intent(Intent.ACTION_SEND);
			intent.putExtra(Intent.EXTRA_TEXT, Uri.parse(url).getQueryParameter("text"));
			intent.setType("text/plain");
			intent.setPackage("com.whatsapp");
			activity.startActivity(intent);
			return true;
		} else if (url.startsWith("geo:")) {
			intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			intent.setPackage("com.google.android.apps.maps");
			if (intent.resolveActivity(packageManager) != null) {
				activity.startActivity(intent);
				return true;
			} else {
				return false;
			}
		} else if (url.startsWith("intent:")) {
			try {
				intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
				if (intent.resolveActivity(packageManager) != null) {
					activity.startActivity(intent);
					return true;
				}
				String fallbackUrl = intent.getStringExtra("browser_fallback_url");
				if (fallbackUrl != null) {
					webView.loadUrl(fallbackUrl);
				}
				intent = new Intent(Intent.ACTION_VIEW)
						.setData(Uri.parse("market://details?id=" + intent.getPackage()));
				if (intent.resolveActivity(packageManager) != null) {
					activity.startActivity(intent);
					return true;
				}
				return false;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}

	@SimpleProperty(description = "Returns whether deep links are enabled or not")
	public boolean DeepLinks() {
		return deepLinks;
	}

	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
	@SimpleProperty(description = "Sets whether to enable deep links or not i.e. tel: , whatsapp: , sms: , etc.")
	public void DeepLinks(boolean d) {
		deepLinks = d;
	}

	@SimpleProperty(description = "Returns whether to load content in desktop mode")
	public boolean DesktopMode() {
		return desktopMode;
	}

	@SimpleProperty(description = "Sets whether to load content in desktop mode")
	public void DesktopMode(boolean mode) {
		if (mode) {
			UserAgent = UserAgent.replace("Android", "diordnA").replace("Mobile", "eliboM");
		} else {
			UserAgent = UserAgent.replace("diordnA", "Android").replace("eliboM", "Mobile");
		}
		webView.getSettings().setUserAgentString(UserAgent);
		desktopMode = mode;
	}

	@SimpleFunction(description = "Dismiss previously requested Js alert")
	public void DismissJsAlert() {
		if (jsAlert != null) {
			jsAlert.cancel();
			jsAlert = null;
		}
	}

	@SimpleProperty(description = "Gets whether the WebView should display on-screen zoom controls")
	public boolean DisplayZoom() {
		return displayZoom;
	}

	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
	@SimpleProperty(description = "Sets whether the WebView should display on-screen zoom controls")
	public void DisplayZoom(boolean bool) {
		displayZoom = bool;
	}

	@SimpleFunction(description = "Downloads the given file")
	public void Download(String url, String mimeType, String contentDisposition, String fileName, String downloadDir) {
		if (!hasWriteAccess) {
			new Handler().post(new Runnable() {
				@Override
				public void run() {
					form.askPermission("android.permission.WRITE_EXTERNAL_STORAGE", new PermissionResultHandler() {
						@Override
						public void HandlePermissionResponse(String permission, boolean granted) {
							hasWriteAccess = granted;
						}
					});
				}
			});
		}
		if (hasWriteAccess) {
			String name = fileName;
			String dir = downloadDir;
			DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
			request.setMimeType(mimeType);
			String cookies = CookieManager.getInstance().getCookie(url);
			request.addRequestHeader("cookie", cookies);
			request.addRequestHeader("User-Agent", UserAgent);
			request.setDescription("Downloading file...");
			request.setTitle(fileName);
			request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
			if (downloadDir.isEmpty()) {
				dir = Environment.DIRECTORY_DOWNLOADS;
			}
			if (fileName.isEmpty()) {
				name = URLUtil.guessFileName(url, contentDisposition, mimeType);
				request.setTitle(name);
			}
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
				request.setDestinationInExternalFilesDir(context, dir, name);
			} else {
				request.setDestinationInExternalPublicDir(dir, name);
			}
			DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
			dm.enqueue(request);
		}
	}

	@SimpleProperty(description = "Returns whether webview supports JavaScript execution")
	public boolean EnableJS() {
		return webView.getSettings().getJavaScriptEnabled();
	}

	@SimpleProperty(description = "Tells the WebView to enable JavaScript execution.")
	public void EnableJS(boolean js) {
		webView.getSettings().setJavaScriptEnabled(js);
	}

	@SimpleFunction(description = "Asynchronously evaluates JavaScript in the context of the currently displayed page.")
	public void EvaluateJavaScript(String script) {
		webView.evaluateJavascript(script, new ValueCallback<String>() {
			@Override
			public void onReceiveValue(String s) {
				AfterJavaScriptEvaluated(s);
			}
		});
	}

	@SimpleProperty(description = "Returns whether webview can access local files")
	public boolean FileAccess() {
		return webView.getSettings().getAllowFileAccess();
	}

	@SimpleProperty(description = "Sets whether webview can access local files.Use this to enable file uploading and loading files using HTML")
	public void FileAccess(boolean allowfiles) {
		if (allowfiles && !hasWriteAccess) {
			new Handler().post(new Runnable() {
				@Override
				public void run() {
					form.askPermission("android.permission.WRITE_EXTERNAL_STORAGE", new PermissionResultHandler() {
						@Override
						public void HandlePermissionResponse(String permission, boolean granted) {
							hasWriteAccess = granted;
						}
					});
				}
			});
		}
		webView.getSettings().setAllowFileAccess(allowfiles && hasWriteAccess);
		webView.getSettings().setAllowFileAccessFromFileURLs(allowfiles && hasWriteAccess);
		webView.getSettings().setAllowUniversalAccessFromFileURLs(allowfiles && hasWriteAccess);
		webView.getSettings().setAllowContentAccess(allowfiles && hasWriteAccess);
	}

	@SimpleEvent(description = "Event raised when file uploading is needed")
	public void FileUploadNeeded(String mimeType, boolean isCaptureEnabled) {
		EventDispatcher.dispatchEvent(this, "FileUploadNeeded", mimeType, isCaptureEnabled);
	}

	@SimpleFunction(description = "Finds all instances of find on the page and highlights them, asynchronously. Successive calls to this will cancel any pending searches.")
	public void Find(String string) {
		webView.findAllAsync(string);
	}

	@SimpleFunction(description = "Highlights and scrolls to the next match if 'forward' is true else scrolls to previous match.")
	public void FindNext(boolean forward) {
		webView.findNext(forward);
	}

	@SimpleEvent(description = "Event raised after 'Find' method with int 'activeMatchOrdinal','numberOfMatches' and 'isDoneCounting'")
	public void FindResultReceived(int activeMatchOrdinal, int numberOfMatches, boolean isDoneCounting) {
		EventDispatcher.dispatchEvent(this, "FindResultReceived", activeMatchOrdinal, numberOfMatches, isDoneCounting);
	}

	@SimpleProperty(description = "Determines whether to follow links when they are tapped in the WebViewer."
			+ "If you follow links, you can use GoBack and GoForward to navigate the browser history")
	public boolean FollowLinks() {
		return followLinks;
	}

	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
	@SimpleProperty(description = "Sets whether to follow links or not")
	public void FollowLinks(boolean follow) {
		followLinks = follow;
	}

	@SimpleProperty(description = "Returns the font size of text")
	public int FontSize() {
		return webView.getSettings().getDefaultFontSize();
	}

	@SimpleProperty(description = "Sets the default font size of text. The default is 16.")
	public void FontSize(int size) {
		webView.getSettings().setDefaultFontSize(size);
	}

	@SimpleFunction(description = "Get cookies for specific url")
	public String GetCookies(String url) {
		String cookies = CookieManager.getInstance().getCookie(url);
		return cookies != null ? cookies : "";
	}

	@SimpleFunction(description = "Return the scrolled left position of the webview")
	public int GetScrollX() {
		return d2p(webView.getScrollX());
	}

	@SimpleFunction(description = "Return the scrolled top position of the webview")
	public int GetScrollY() {
		return d2p(webView.getScrollY());
	}

	@SimpleFunction(description = "Gets the SSL certificate for the main top-level page and raises 'GotCertificate' event")
	public void GetSslCertificate() {
		SslCertificate certificate = webView.getCertificate();
		if (certificate != null) {
			GotCertificate(true, certificate.getIssuedBy().getDName(), certificate.getIssuedTo().getDName(),
					certificate.getValidNotAfterDate().toString());
		} else {
			GotCertificate(false, "", "", "");
		}
	}

	@SimpleFunction(description = "Goes back in the history of this WebView.")
	public void GoBack() {
		if (CanGoBack()) {
			webView.goBack();
		}
	}

	@SimpleFunction(description = "Goes to the history item that is the number of steps away from the current item. Steps is negative if backward and positive if forward.")
	public void GoBackOrForward(int steps) {
		if (CanGoBackOrForward(steps)) {
			webView.goBackOrForward(steps);
		}
	}

	@SimpleFunction(description = "Goes forward in the history of this WebView.")
	public void GoForward() {
		if (CanGoForward()) {
			webView.goForward();
		}
	}

	@SimpleEvent(description = "Event raised after getting SSL certificate of current displayed url/website with boolean 'isSecure' and Strings 'issuedBy','issuedTo' and 'validTill'.If 'isSecure' is false and other values are empty then assume that website is not secure")
	public void GotCertificate(boolean isSecure, String issuedBy, String issuedTo, String validTill) {
		EventDispatcher.dispatchEvent(this, "GotCertificate", isSecure, issuedBy, issuedTo, validTill);
	}

	@SimpleFunction(description = "Loads the given URL.")
	public void GoToUrl(String url) {
		CancelJsRequests();
		webView.loadUrl(url);
	}

	@SimpleEvent(description = "Event raised after getting previus print's result.")
	public void GotPrintResult(String print, boolean isCompleted, boolean isFailed, boolean isBlocked) {
		EventDispatcher.dispatchEvent(this, "GotPrintResult", print, isCompleted, isFailed, isBlocked);
	}

	@SimpleFunction(description = "Grants given permissions to webview.Use empty list to deny the request.")
	public void GrantPermission(final List<String> permissions) {
		if (permissionRequest != null) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (permissions.isEmpty()) {
						permissionRequest.deny();
					} else {
						Object[] objArr = permissions.toArray();
						String[] str = Arrays.copyOf(objArr, objArr.length, String[].class);
						permissionRequest.grant(str);
					}
					permissionRequest = null;
				}
			});
		}
	}

	@SimpleFunction(description = "Hides previously shown custom view")
	public void HideCustomView() {
		webView.getWebChromeClient().onHideCustomView();
	}

	@SimpleProperty(description = "Returns whether webview ignores SSL errors", category = PropertyCategory.BEHAVIOR)
	public boolean IgnoreSslErrors() {
		return ignoreSslErrors;
	}

	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
	@SimpleProperty(description = "Determine whether or not to ignore SSL errors. Set to true to ignore "
			+ "errors. Use this to accept self signed certificates from websites")
	public void IgnoreSslErrors(boolean ignore) {
		ignoreSslErrors = ignore;
	}

	@SimpleProperty(description = "Sets the initial scale for active WebView. 0 means default. If initial scale is greater than 0, WebView starts with this value as initial scale.")
	public void InitialScale(int scale) {
		webView.setInitialScale(scale);
	}

	@SimpleFunction(description = "Creates the webview in given arrangement with id")
	public void InitWebView(HVArrangement container) {
		if (container != null && isInit == false) {
			FrameLayout frameLayout = (FrameLayout) container.getView();
			frameLayout.addView(webView, new FrameLayout.LayoutParams(-1, -1));
			isInit = true;
		}

	}

	@SimpleFunction(description = "Invokes the graphical zoom picker widget for this WebView. This will result in the zoom widget appearing on the screen to control the zoom level of this WebView.Note that it does not checks whether zoom is enabled or not.")
	public void InvokeZoomPicker() {
		webView.invokeZoomPicker();
	}

	@SimpleProperty(description = "")
	public int LayerType() {
		return webView.getLayerType();
	}

	@SimpleProperty(description = "")
	public void LayerType(int type) {
		webView.setLayerType(type, null);
	}

	@SimpleFunction(description = "Loads the given data into this WebView using a 'data' scheme URL.")
	public void LoadHtml(String html) {
		CancelJsRequests();
		webView.loadData(html, "text/html", "UTF-8");
	}

	@SimpleFunction(description = "Loads the given URL with the specified additional HTTP headers defined is list of lists.")
	public void LoadWithHeaders(String url, List<List<String>> headers) {
		if (headers.size() != 0 && headers.get(0).size() == 2) {
			java.util.Map<String, String> header = new HashMap<String, String>();
			for (List<String> list : headers) {
				header.put(list.get(0), list.get(1));
			}
			webView.loadUrl(url, header);
		} else {
			GoToUrl(url);
		}
	}

	@SimpleProperty(description = "Returns whether the WebView loads pages in overview mode")
	public boolean LoadWithOverviewMode() {
		return webView.getSettings().getLoadWithOverviewMode();
	}

	@SimpleProperty(description = "Sets whether the WebView loads pages in overview mode, that is, zooms out the content to fit on screen by width. This setting is taken into account when the content width is greater than the width of the WebView control.")
	public void LoadWithOverviewMode(boolean bool) {
		webView.getSettings().setLoadWithOverviewMode(bool);
	}

	@SimpleProperty(description = "Returns whether text selection and context menu are enabled or not")
	public boolean LongClickable() {
		return !webView.isLongClickable();
	}

	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
	@SimpleProperty(description = "Sets whether to enable text selection and context menu")
	public void LongClickable(boolean bool) {
		webView.setLongClickable(!bool);
	}

	@SimpleEvent(description = "Event raised when something is long clicked in webview with item(image,string,empty,etc) and type(item type like 0,1,8,etc)")
	public void LongClicked(String item, String secondaryUrl, int type) {
		EventDispatcher.dispatchEvent(this, "LongClicked", item, secondaryUrl, type);
	}

	@SimpleEvent(description = "Event triggered when a window needs to be closed")
	public void OnCloseWindowRequest() {
		EventDispatcher.dispatchEvent(this, "OnCloseWindowRequest");
	}

	@SimpleEvent(description = "Event raised after getting console message.")
	public void OnConsoleMessage(String message, int lineNumber, int source, String level) {
		EventDispatcher.dispatchEvent(this, "OnConsoleMessage", message, lineNumber, source, level);
	}

	@SimpleEvent(description = "Event raised when downloading is needed.")
	public void OnDownloadNeeded(String url, String contentDisposition, String mimeType, long size) {
		EventDispatcher.dispatchEvent(this, "OnDownloadNeeded", url, contentDisposition, mimeType, size);
	}

	@SimpleEvent(description = "Event raised when any error is received during loading url and returns message,error code and failing url")
	public void OnErrorReceived(String message, int errorCode, String url) {
		EventDispatcher.dispatchEvent(this, "OnErrorReceived", message, errorCode, url);
	}

	@SimpleEvent(description = "Event raised when resubmission of form is needed")
	public void OnFormResubmission() {
		EventDispatcher.dispatchEvent(this, "OnFormResubmission");
	}

	@SimpleEvent(description = "Event raised when current page exits from full screen mode")
	public void OnHideCustomView() {
		EventDispatcher.dispatchEvent(this, "OnHideCustomView");
	}

	@SimpleEvent(description = "Event raised when Js have to show an alert to user")
	public void OnJsAlert(String url, String message) {
		EventDispatcher.dispatchEvent(this, "OnJsAlert", url, message);
	}

	@SimpleEvent(description = "Tells to display a confirm dialog to the user.")
	public void OnJsConfirm(String url, String message) {
		EventDispatcher.dispatchEvent(this, "OnJsConfirm", url, message);
	}

	@SimpleEvent(description = "Event raised when JavaScript needs input from user")
	public void OnJsPrompt(String url, String message, String defaultValue) {
		EventDispatcher.dispatchEvent(this, "OnJsPrompt", url, message, defaultValue);
	}

	@SimpleEvent(description = "Event raised when new window is requested by webview with boolean 'isDialog' and 'isPopup'")
	public void OnNewWindowRequest(String url, boolean isDialog, boolean isPopup) {
		EventDispatcher.dispatchEvent(this, "OnNewWindowRequest", url, isDialog, isPopup);
	}

	@SimpleEvent(description = "Event raised when a website asks for specific permission(s) in list format.")
	public void OnPermissionRequest(List<String> permissionsList) {
		EventDispatcher.dispatchEvent(this, "OnPermissionRequest", permissionsList);
	}

	@SimpleEvent(description = "Event raised when page loading progress has changed.")
	public void OnProgressChanged(int progress) {
		EventDispatcher.dispatchEvent(this, "OnProgressChanged", progress);
	}

	@SimpleEvent(description = "Notifies that the WebView received an HTTP authentication request.")
	public void OnReceivedHttpAuthRequest(String host, String realm) {
		EventDispatcher.dispatchEvent(this, "OnReceivedHttpAuthRequest", host, realm);
	}

	@SimpleEvent(description = "Event raised when webview gets scrolled")
	public void OnScrollChanged(int scrollX, int scrollY, int oldScrollX, int oldScrollY, boolean canGoLeft,
			boolean canGoRight) {
		EventDispatcher.dispatchEvent(this, "OnScrollChanged", scrollX, scrollY, oldScrollX, oldScrollY, canGoLeft,
				canGoRight);
	}

	@SimpleEvent(description = "Event raised when current page enters in full screen mode")
	public void OnShowCustomView() {
		EventDispatcher.dispatchEvent(this, "OnShowCustomView");
	}

	@SimpleProperty(description = "")
	public int OverScrollMode() {
		return webView.getOverScrollMode();
	}

	@SimpleProperty(description = "")
	public void OverScrollMode(int mode) {
		webView.setOverScrollMode(mode);
	}

	public int p2d(int p) {
		return Math.round(p * deviceDensity);
	}

	@SimpleFunction(description = "Scrolls the contents of the WebView down by half the page size")
	public void PageDown(boolean bottom) {
		webView.pageDown(bottom);
	}

	@SimpleEvent(description = "Event raised when page loading has finished.")
	public void PageLoaded() {
		EventDispatcher.dispatchEvent(this, "PageLoaded");
	}

	@SimpleEvent(description = "Event indicating that page loading has started in web view.")
	public void PageStarted(String url) {
		EventDispatcher.dispatchEvent(this, "PageStarted", url);
	}

	@SimpleFunction(description = "Scrolls the contents of the WebView up by half the page size")
	public void PageUp(boolean top) {
		webView.pageUp(top);
	}

	@SimpleFunction(description = "Does a best-effort attempt to pause any processing that can be paused safely, such as animations and geolocation. Note that this call does not pause JavaScript.")
	public void PauseWebView() {
		webView.onPause();
	}

	@SimpleFunction(description = "Loads the URL with postData using 'POST' method into active WebView.")
	public void PostData(String url, String data) {
		webView.postUrl(url, data.getBytes(StandardCharsets.UTF_8));
	}

	@SimpleFunction(description = "Prints the content of webview with given document name")
	public void PrintWebContent(String documentName) throws Exception {
		PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
		if (documentName.isEmpty()) {
			jobName = webView.getTitle() + "_Document";
		} else {
			jobName = documentName;
		}
		PrintDocumentAdapter printAdapter = new PrintDocumentAdapterWrapper(
				webView.createPrintDocumentAdapter(jobName));
		if (printManager != null) {
			printJob = printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());
		}
	}

	@SimpleFunction(description = "Instructs the WebView to proceed with the authentication with the given credentials.If both parameters are empty then it will cancel the request.")
	public void ProceedHttpAuthRequest(String username, String password) {
		if (httpAuthHandler != null) {
			if (username.isEmpty() && password.isEmpty()) {
				httpAuthHandler.cancel();
			} else {
				httpAuthHandler.proceed(username, password);
			}
			httpAuthHandler = null;
		}
	}

	@SimpleProperty(description = "Returns whether webview will prompt for permission and raise 'OnPermissionRequest' event or not")
	public boolean PromptForPermission() {
		return prompt;
	}

	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
	@SimpleProperty(description = "Sets whether webview will prompt for permission and raise 'OnPermissionRequest' event or not else assume permission is granted.")
	public void PromptForPermission(boolean pr) {
		prompt = pr;
	}

	@SimpleFunction(description = "Reloads the current URL.")
	public void Reload() {
		CancelJsRequests();
		webView.reload();
	}

	public void resetWebView(final WebView web) {
		web.addJavascriptInterface(wvInterface, "AppInventor");
		MOBILE_USER_AGENT = web.getSettings().getUserAgentString();
		web.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
		web.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
		web.setFocusable(true);
		web.setWebViewClient(new WebClient());
		web.setWebChromeClient(new ChromeClient());
		web.getSettings().setJavaScriptEnabled(true);
		web.getSettings().setDisplayZoomControls(displayZoom);
		web.getSettings().setAllowFileAccess(false);
		web.getSettings().setAllowFileAccessFromFileURLs(false);
		web.getSettings().setAllowUniversalAccessFromFileURLs(false);
		web.getSettings().setAllowContentAccess(false);
		web.getSettings().setSupportZoom(zoomEnabled);
		web.getSettings().setBuiltInZoomControls(zoomEnabled);
		web.setLongClickable(false);
		web.getSettings().setTextZoom(zoomPercent);
		cookieManager.setAcceptThirdPartyCookies(web, true);
		web.getSettings().setDomStorageEnabled(true);
		web.setVerticalScrollBarEnabled(true);
		web.setHorizontalScrollBarEnabled(true);
		web.getSettings().setDefaultFontSize(16);
		web.getSettings().setBlockNetworkImage(false);
		web.getSettings().setLoadsImagesAutomatically(true);
		web.getSettings().setLoadWithOverviewMode(true);
		web.getSettings().setUseWideViewPort(true);
		web.getSettings().setBlockNetworkLoads(false);
		web.getSettings().setMediaPlaybackRequiresUserGesture(false);
		web.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		web.getSettings().setSupportMultipleWindows(true);
		web.getSettings().setGeolocationDatabasePath(null);
		web.getSettings().setDatabaseEnabled(false);
		web.getSettings().setGeolocationEnabled(false);
		if (UserAgent.isEmpty()) {
			UserAgent = MOBILE_USER_AGENT;
		}
		web.getSettings().setUserAgentString(UserAgent);
		web.setDownloadListener(new DownloadListener() {
			@Override
			public void onDownloadStart(String s, String s1, String s2, String s3, long l) {
				OnDownloadNeeded(s, s2, s3, l);
			}
		});
		web.setFindListener(new WebView.FindListener() {
			@Override
			public void onFindResultReceived(int i, int i1, boolean b) {
				FindResultReceived(i, i1, b);
			}
		});
		web.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_UP:
					if (!v.hasFocus()) {
						v.requestFocus();
					}
					break;
				}
				return false;
			}
		});
		web.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				final WebView.HitTestResult hitTestResult = webView.getHitTestResult();
				String item = hitTestResult.getExtra();
				int type = hitTestResult.getType();
				if (type != WebView.HitTestResult.UNKNOWN_TYPE) {
					if (item == null) {
						item = "";
					}
					String str = "";
					if (type == 8) {
						Message message = new Handler().obtainMessage();
						web.requestFocusNodeHref(message);
						str = (String) message.getData().get("url");
					}
					LongClicked(item, str, type);
					return webView.isLongClickable();
				}
				return false;
			}
		});
		web.setOnScrollChangeListener(new View.OnScrollChangeListener() {
			@Override
			public void onScrollChange(View view, int i, int i1, int i2, int i3) {
				OnScrollChanged(i, i1, i2, i3, web.canScrollHorizontally(-1), web.canScrollHorizontally(1));
			}
		});
	}

	@SimpleFunction(description = "Restarts current/previous print job. You can request restart of a failed print job.")
	public void RestartPrinting() throws Exception {
		printJob.restart();
	}

	@SimpleFunction(description = "Whether to resubmit form or not.")
	public void ResubmitForm(boolean reSubmit) {
		if (reSend != null && dontSend != null) {
			if (reSubmit) {
				reSend.sendToTarget();
			} else {
				dontSend.sendToTarget();
			}
			reSend = null;
			dontSend = null;
		}
	}

	@SimpleFunction(description = "Resumes the previously paused WebView.")
	public void ResumeWebView(int id) {
		webView.onResume();
	}

	@SimpleProperty()
	public float RotationAngle() {
		return webView.getRotation();
	}

	@SimpleProperty()
	public void RotationAngle(float rotation) {
		webView.setRotation(rotation);
	}

	@SimpleFunction(description = "Saves the current site as a web archive")
	public void SaveArchive(String dir) {
		webView.saveWebArchive(dir, true, new ValueCallback<String>() {
			@Override
			public void onReceiveValue(String s) {
				if (s == null) {
					AfterArchiveSaved(false, "");
				} else {
					AfterArchiveSaved(true, s);
				}
			}
		});
	}

	@SimpleProperty(description = "Whether to display horizonatal and vertical scrollbars or not")
	public void ScrollBar(boolean bool) {
		webView.setVerticalScrollBarEnabled(bool);
		webView.setHorizontalScrollBarEnabled(bool);
	}

	@SimpleProperty(description = "")
	public int ScrollBarStyle() {
		return webView.getScrollBarStyle();
	}

	@SimpleProperty(description = "")
	public void ScrollBarStyle(int style) {
		webView.setScrollBarStyle(style);
	}

	@SimpleFunction(description = "Scrolls the webview to given position")
	public void ScrollTo(final int x, final int y) {
		webView.postDelayed(new Runnable() {
			@Override
			public void run() {
				webView.scrollTo(p2d(x), p2d(y));
			}
		}, 300);
	}

	@SimpleFunction(description = "Sets cookies for given url")
	public void SetCookies(String url, String cookieString) {
		try {
			CookieManager.getInstance().setCookie(url, cookieString);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SimpleFunction(description = "Sets the visibility of webview by id")
	public void SetVisibility(boolean visibility) {
		if (visibility) {
			webView.setVisibility(View.VISIBLE);
		} else {
			webView.setVisibility(View.GONE);
		}

	}

	@SimpleFunction(description = "Stops the current load.")
	public void StopLoading() {
		webView.stopLoading();
	}

	@SimpleFunction(description = "Uploads the given file from content uri.Use empty string to cancel the upload request.")
	public void UploadFile(String contentUri) {
		if (mFilePathCallback != null) {
			if (contentUri.isEmpty()) {
				mFilePathCallback.onReceiveValue(null);
				mFilePathCallback = null;
			} else {
				mFilePathCallback.onReceiveValue(new Uri[] { Uri.parse(contentUri) });
				mFilePathCallback = null;
			}
		}
	}

	@SimpleProperty(description = "Get webview user agent", category = PropertyCategory.BEHAVIOR)
	public String UserAgent() {
		return UserAgent;
	}

	@SimpleProperty(description = "Sets the WebView's user-agent string. If the string is null or empty, the system default value will be used. ")
	public void UserAgent(String userAgent) {
		if (!userAgent.isEmpty()) {
			UserAgent = userAgent;
		} else {
			UserAgent = MOBILE_USER_AGENT;
		}
		webView.getSettings().setUserAgentString(UserAgent);
	}

	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
	@SimpleProperty(description = "Whether or not to give the application permission to use the Javascript geolocation API")
	public void UsesLocation(boolean uses) {
		if (uses && !hasLocationAccess) {
			new Handler().post(new Runnable() {
				@Override
				public void run() {
					form.askPermission("android.permission.ACCESS_FINE_LOCATION", new PermissionResultHandler() {
						@Override
						public void HandlePermissionResponse(String permission, boolean granted) {
							hasLocationAccess = granted;
						}
					});
				}
			});
		}
		webView.getSettings().setGeolocationEnabled(uses && hasLocationAccess);
	}

	@SimpleProperty(description = "Returns whether the WebView should enable support for the 'viewport' HTML meta tag or should use a wide viewport.")
	public boolean UseWideViewPort() {
		return webView.getSettings().getUseWideViewPort();
	}

	@SimpleProperty(description = "Sets whether the WebView should enable support for the 'viewport' HTML meta tag or should use a wide viewport.")
	public void UseWideViewPort(boolean bool) {
		webView.getSettings().setUseWideViewPort(bool);
	}

	@SimpleProperty(description = "Returns the visibility of current webview")
	public boolean Visible() {
		return webView.getVisibility() == View.VISIBLE;
	}

	@SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Get webview string")
	public String WebViewString() {
		return wvInterface.webViewString;
	}

	@SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Set webview string")
	public void WebViewString(String newString) {
		wvInterface.setWebViewStringFromBlocks(newString);
	}

	@SimpleEvent(description = "When the JavaScript calls AppInventor.setWebViewString this event is run.")
	public void WebViewStringChanged(String value) {
		EventDispatcher.dispatchEvent(this, "WebViewStringChanged", value);
	}

	@SimpleFunction(description = "Performs a zoom operation in the WebView by given zoom percent")
	public void ZoomBy(int zoomP) {
		webView.zoomBy(zoomP);
	}

	@SimpleProperty(description = "Gets whether the WebView should support zooming using its on-screen zoom controls and gestures")
	public boolean ZoomEnabled() {
		return zoomEnabled;
	}

	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
	@SimpleProperty(description = "Sets whether the WebView should support zooming using its on-screen zoom controls and gestures")
	public void ZoomEnabled(boolean bool) {
		zoomEnabled = bool;
	}

	@SimpleFunction(description = "Performs zoom in in the WebView")
	public void ZoomIn() {
		webView.zoomIn();
	}

	@SimpleFunction(description = "Performs zoom out in the WebView")
	public void ZoomOut() {
		webView.zoomOut();
	}

	@SimpleProperty(description = "Gets the zoom of the page in percent")
	public int ZoomPercent() {
		return zoomPercent;
	}

	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER, defaultValue = "100")
	@SimpleProperty(description = "Sets the zoom of the page in percent. The default is 100")
	public void ZoomPercent(int zoom) {
		zoomPercent = zoom;
	}
}