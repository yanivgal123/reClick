package com.reclick.reclick;

import java.util.concurrent.ExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import com.reclick.framework.App;
import com.reclick.framework.Prefs;
import com.reclick.request.Request;
import com.reclick.request.Request.RequestObject;
import com.reclick.request.Request.RequestType;
import com.reclick.request.Urls;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LoginActivity extends Activity {
	
	private final String TAG = this.getClass().getSimpleName();
	
	private LinearLayout mainLayout;
	private EditText usernameInput;
	private EditText passwordInput;
	private TextView nicknameLabel;
	private EditText nicknameInput;
	private Button loginBtn;
	private Button signUpBtn;
	private TextView loginLink;
	private TextView signUpLink;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		
		mainLayout = (LinearLayout) findViewById(R.id.login_main_content);
		mainLayout.bringToFront();
		
		usernameInput = (EditText) findViewById(R.id.login_activity_username_input);
		passwordInput = (EditText) findViewById(R.id.login_activity_password_input);
		nicknameLabel = (TextView) findViewById(R.id.login_activity_nickname_label);
		nicknameInput = (EditText) findViewById(R.id.login_activity_nickname_input);
		loginBtn = (Button) findViewById(R.id.login_activity_login_btn);
		signUpBtn = (Button) findViewById(R.id.login_activity_signup_btn);
		loginLink = (TextView) findViewById(R.id.login_activity_login_here_link);
		signUpLink = (TextView) findViewById(R.id.login_activity_sign_up_here_link);
	}
	
	public void login(View v) {
		App.hideSoftKeyboard(this);
		
		String username = usernameInput.getText().toString();
		String password = passwordInput.getText().toString();
		
		if (username.isEmpty() || password.isEmpty()) {
			App.showToast(this, "Please fill both fields");
			return;
		}
		
		JSONObject response = sendLoginRequest(
				username,
				password,
				Prefs.getGcmRegId(this)
				);
		
		try {
			if (response.getString("status").equals("success")) {
				
				Prefs.setUsername(this, response.getString("username"));
				Prefs.setNickname(this, response.getString("nickname"));
				
				
				Intent intent = new Intent(
						this, com.reclick.reclick.MainActivity.class);
				startActivity(intent);
				finish();
			}
			App.showToast(this, response.getString("message"));
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage());
		}
	}
	
	public void signup(View v) {
		App.hideSoftKeyboard(this);
		
		String username = usernameInput.getText().toString();
		String password = passwordInput.getText().toString();
		String nickname = nicknameInput.getText().toString();
		
		if (username.isEmpty() || password.isEmpty() || nickname.isEmpty()) {
			App.showToast(this, "Please fill all fields");
			return;
		}
		
		JSONObject response = sendSignupRequest(
				username,
				password,
				nickname,
				Prefs.getGcmRegId(this)
				);
		
		try {
			if (response.getString("status").equals("success")) {
				Prefs.setUsername(this, response.getString("username"));
				Prefs.setNickname(this, response.getString("nickname"));
				
				Intent intent = new Intent(
						this, com.reclick.reclick.MainActivity.class);
				startActivity(intent);
				finish();
			}
			App.showToast(this, response.getString("message"));
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage());
		}
	}
	
	public void loginLink(View v) {
		nicknameLabel.setVisibility(View.GONE);
		nicknameInput.setVisibility(View.GONE);
		signUpBtn.setVisibility(View.GONE);
		loginLink.setVisibility(View.GONE);
		
		loginBtn.setVisibility(View.VISIBLE);
		signUpLink.setVisibility(View.VISIBLE);
	}
	
	public void signUpLink(View v) {
		loginBtn.setVisibility(View.GONE);
		signUpLink.setVisibility(View.GONE);
		
		nicknameLabel.setVisibility(View.VISIBLE);
		nicknameInput.setVisibility(View.VISIBLE);
		signUpBtn.setVisibility(View.VISIBLE);
		loginLink.setVisibility(View.VISIBLE);
	}
	
	private JSONObject sendLoginRequest(String username, String password, String gcmRegId) {
		JSONObject response = null;
		
		RequestObject ro = new RequestObject(Urls.login(this), RequestType.POST);
		ro.addParameter("username", username);
		ro.addParameter("password", App.md5(password));
		ro.addParameter("gcmRegId", gcmRegId);
		
		try {
			response = new Request(ro).execute().get();
		} catch (InterruptedException e) {
			Log.e(TAG, e.getMessage());
		} catch (ExecutionException e) {
			Log.e(TAG, e.getMessage());
		}
		
		return response;
	}
	
	private JSONObject sendSignupRequest(String username, String password, String nickname, String gcmRegId) {
		JSONObject response = null;
		
		RequestObject ro = new RequestObject(Urls.signup(this), RequestType.POST);
		ro.addParameter("username", username);
		ro.addParameter("password", App.md5(password));
		ro.addParameter("nickname", nickname);
		ro.addParameter("gcmRegId", gcmRegId);
		
		try {
			response = new Request(ro).execute().get();
		} catch (InterruptedException e) {
			Log.e(TAG, e.getMessage());
		} catch (ExecutionException e) {
			Log.e(TAG, e.getMessage());
		}
		
		return response;
	}
}