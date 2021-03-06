package com.reclick.reclick;

import org.apache.http.HttpStatus;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import unite.Client;
import unite.OnResponseListener;
import unite.Response;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.reclick.framework.Prefs;
import com.reclick.request.Urls;

public class GamesAdapter extends BaseAdapter {
	
	private Context context;
	private JSONArray games;
	private boolean isCurrUserGames;
	
	private String gameId;
	private String sequence;
	private boolean started;
	
	public GamesAdapter(Context context, JSONArray games, boolean isCurrUserGames) {
		super();
		this.context = context;
		this.games = games;
		this.isCurrUserGames = isCurrUserGames;
	}
	
	@Override
	public int getCount() {
		return games.length();
	}

	@Override
	public Object getItem(int position) {
		try {
			return games.get(position);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public long getItemId(int position) { // returns the game id of the game in the given position.
		try {
			return Long.parseLong(((JSONObject)games.get(position)).getString("id"));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public void add(JSONObject game) {
		games.put(game);
		notifyDataSetChanged();
	}
	
	public void updateGames(JSONArray games) {
		this.games = games;
		notifyDataSetChanged();
	}
	
	private class ViewHolder {
		String gameId;
		String sequence;
		boolean started;
		TextView gameName;
		TextView gameDescription;
		Button joinOrPlay;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.main_games_row, null);
			holder = new ViewHolder();
			holder.gameName = (TextView) convertView.findViewById(R.id.main_activity_games_row_game_name);
			holder.gameDescription = (TextView) convertView.findViewById(R.id.main_activity_games_row_game_description);
			holder.joinOrPlay = (Button) convertView.findViewById(R.id.main_activity_join_or_enter_game_button);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		try {
			JSONObject currentGame = (JSONObject) games.get(position);
			holder.gameName.setText(currentGame.getString("name"));
			holder.gameDescription.setText(currentGame.getString("description"));
			holder.gameId = currentGame.getString("id");
			holder.sequence = currentGame.getString("sequence");
			String started = currentGame.getString("started");
			holder.started = started.equals("true") ? true : false;
			if (isCurrUserGames) {
				if (!currentGame.has("players")) {
					holder.joinOrPlay.setVisibility(View.INVISIBLE);
					return convertView;
				}
				JSONArray players = currentGame.getJSONArray("players");
				int turnInPlayer = getThisPlayerTurn(players);
				int turnInGame = currentGame.getInt("turn");
				if (!holder.started || turnInPlayer != turnInGame) {
					holder.joinOrPlay.setVisibility(View.INVISIBLE);
				} else {
					holder.joinOrPlay.setText("Play");
					holder.joinOrPlay.setVisibility(View.VISIBLE);
				}
			}
			if (holder.joinOrPlay.getVisibility() == View.VISIBLE) {
				holder.joinOrPlay.setOnClickListener(onClickListener);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return convertView;
	}
	
	private int getThisPlayerTurn(JSONArray players) {
		for (int i = 0; i < players.length(); i++) {
			try {
				JSONObject currentPlayer = (JSONObject) players.get(i);
				if (Prefs.getUsername(context).equals(currentPlayer.getString("username"))) {
					return currentPlayer.getInt("turn");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}

	private OnClickListener onClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View view) {
			ViewHolder vh = ((ViewHolder) ((View) view.getParent()).getTag()); 
			gameId = vh.gameId;
			sequence = vh.sequence;
			started = vh.started;
			
			if (isCurrUserGames) { // in case user want's to enter to one of his games.
				Intent intent = new Intent(context, GameActivity.class);
				intent.putExtra("gameId", gameId);
				intent.putExtra("sequence", sequence);
				intent.putExtra("started", started);
				context.startActivity(intent);
				((Activity) context).finish();
			} else { // in case user want's to join to one of the open games list.
				new Client()
					.post(Urls.addPlayerToGame(context, gameId, Prefs.getUsername(context)))
					.setHeader(HTTP.CONTENT_TYPE, context.getString(R.string.application_json))
					.send(onPlayerAddedListener);
			}
		}
	};
	
	private OnResponseListener onPlayerAddedListener = new OnResponseListener() {
		
		@Override
		public void onResponseReceived(Response response) {
			if (response.getStatusCode() == HttpStatus.SC_OK) {
				new Client()
					.post(Urls.startGame(context, gameId))
					.setHeader(HTTP.CONTENT_TYPE, context.getString(R.string.application_json))
					.send(onGameStartedListener);
			}
		}
	};
	
	private OnResponseListener onGameStartedListener = new OnResponseListener() {
		
		@Override
		public void onResponseReceived(Response response) {
			if (response.getStatusCode() == HttpStatus.SC_OK) {
				Intent intent = new Intent(context, GameActivity.class);
				intent.putExtra("gameId", gameId);
				intent.putExtra("sequence", sequence);
				intent.putExtra("started", started);
				context.startActivity(intent);
				((Activity) context).finish();
			}
		}
	};
}