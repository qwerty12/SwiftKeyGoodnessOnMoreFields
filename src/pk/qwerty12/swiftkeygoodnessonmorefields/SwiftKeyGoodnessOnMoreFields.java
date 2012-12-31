package pk.qwerty12.swiftkeygoodnessonmorefields;

import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class SwiftKeyGoodnessOnMoreFields implements IXposedHookLoadPackage {

	private static final String PACKAGE_SWIFTKEY = "com.touchtype.swiftkey"; 
	@Override
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
		if (lpparam.packageName.equals(PACKAGE_SWIFTKEY)) {
			try {
				XposedHelpers.findAndHookMethod("com.touchtype.keyboard.inputeventmodel.KeyboardStateImpl", lpparam.classLoader, "notifyNewEditorInfo", EditorInfo.class, boolean.class, boolean.class, new XC_MethodHook() {
	
					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						Class<?> classEditorInfoUtils = XposedHelpers.findClass("com.touchtype.keyboard.inputeventmodel.EditorInfoUtils", lpparam.classLoader);
						Object editorAnnotationInfo = XposedHelpers.callStaticMethod(classEditorInfoUtils, "correctEditorInfo", (EditorInfo) param.args[0]);
						EditorInfo editorInfo = (EditorInfo) XposedHelpers.getObjectField(editorAnnotationInfo, "editorInfo");
	
						//if ((editorInfo.imeOptions & EditorInfo.IME_MASK_ACTION) != EditorInfo.IME_ACTION_SEARCH)
						//	return;
	
						if ((Boolean) XposedHelpers.callStaticMethod(classEditorInfoUtils, "isValidEditorInfo", editorInfo)) {
							XposedHelpers.setBooleanField(param.thisObject, "mEditorIsSearchField", false);
	
							int editorFlags = editorInfo.inputType & EditorInfo.TYPE_MASK_FLAGS;
							int editorVariant = editorInfo.inputType & EditorInfo.TYPE_MASK_VARIATION;
	
							boolean isLicenseValid = (Boolean) XposedHelpers.callMethod(param.thisObject, "isLicenseValid");
							int enabledLanguagePacks = XposedHelpers.getIntField(param.thisObject, "mEnabledLanguagePacks"); 
							boolean extractedTextWorks = XposedHelpers.getBooleanField(param.thisObject, "mExtractedTextWorks");
							boolean textBeforeCursorWorks = XposedHelpers.getBooleanField(param.thisObject, "mTextBeforeCursorWorks");
							boolean enablePredictionsWhenOnlyTextBeforeCursorWorks = XposedHelpers.getBooleanField(editorAnnotationInfo, "enablePredictionsWhenOnlyTextBeforeCursorWorks");
	
							if (isLicenseValid && //Yes, this one stays.
								enabledLanguagePacks > 0 &&
								((editorInfo.inputType & EditorInfo.TYPE_MASK_CLASS) == EditorInfo.TYPE_CLASS_TEXT) &&
								editorVariant != InputType.TYPE_TEXT_VARIATION_PASSWORD &&
								editorVariant != InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD &&
								editorVariant != InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD &&
								((editorFlags & InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE) != InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE) &&							
								((editorFlags & EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS) != EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS) &&
								//editorVariant != InputType.TYPE_TEXT_VARIATION_URI &&
								//editorVariant != InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS &&
								//editorVariant != InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS &&
								//editorVariant != InputType.TYPE_TEXT_VARIATION_FILTER &&
								(extractedTextWorks && (textBeforeCursorWorks || enablePredictionsWhenOnlyTextBeforeCursorWorks)))
							{
								XposedHelpers.setBooleanField(param.thisObject, "mPredictionEnabled", true);
								Object listenerManager = XposedHelpers.getObjectField(param.thisObject, "mListenerManager");
								XposedHelpers.callMethod(listenerManager, "notifyPredictionsEnabledListener", true);
								XposedHelpers.callMethod(listenerManager, "notifyCandidateUpdateListeners");
							}
						}
					}
	
				});
			} catch (Throwable t) { XposedBridge.log(t); }	
		}
	}

}
