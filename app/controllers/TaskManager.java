package controllers;

import play.*;
import play.mvc.*;

import java.text.SimpleDateFormat;
import java.util.*;

import models.*;

/**
 * 利用者用処理
 * 
 * @author maruyama
 */
@Check("MemberOnly")
@With(Secure.class)
public class TaskManager extends Controller {

	/**
	 * プロジェクト一覧画面<br />
	 * 参加しているかどうかは，Viewで判定しつつ表示する
	 */
	public static void projectListScreen() {
		// ログインしているユーザーのユーザー情報を取得する
		UserInformation user = Auth.get_login_user();

		// 初回ログインだったら，パスワード変更画面へ
		if (user.is_first_login == true) {
			passwordChangeScreen();
		}

		// プロジェクト一覧を取得する（削除とかないので，全部取得）
		List<ProjectInformation> project_list = ProjectInformation.findAll();

		// 画面出力
		render(user, project_list);
	}

	/**
	 * パスワード変更画面<br />
	 * 初回ログインだった場合，プロジェクト一覧画面へのアクセスから自動でこちらに飛ぶ
	 */
	public static void passwordChangeScreen() {
		// ログインしているユーザーのユーザー情報を取得する
		UserInformation user = Auth.get_login_user();

		// 画面出力
		render(user);
	}

	/**
	 * パスワード変更処理
	 * 
	 * @param password1
	 *            パスワード
	 * @param password2
	 *            パスワード（再入力）
	 */
	public static void passwordChange(String password1, String password2) {
		if (!password1.equals(password2)) {
			flash.put("error", "パスワードが一致していません");
			passwordChangeScreen();
		}

		// ログインしているユーザーのユーザー情報を取得する
		UserInformation user = Auth.get_login_user();

		// パスワードを暗号化して，上書き
		user.password = UserInformation.get_password_hash(password1);

		// 初回ログインフラグをfalseにする
		user.is_first_login = false;

		// 保存
		user.save();

		flash.put("success", "パスワードを変更しました．");
		projectListScreen();
	}

	/**
	 * タスク一覧画面<br />
	 * 利用した指定したプロジェクトのプロジェクト名を受け取り，そのプロジェクトのタスク一覧を表示する
	 * 
	 * @param project_name
	 *            プロジェクト名
	 */
	public static void taskListScreen(String project_name) {
		// プロジェクト情報を取得
		ProjectInformation project = ProjectInformation.find("byProject_name",
				project_name).first();
		if (project == null) { // プロジェクトが存在しなかったら一覧画面に戻す
			flash.put("error", "プロジェクトは存在しません");
			projectListScreen();
		} else {
			// 初めてのアクセスだったら，参加させる
			UserInformation user = Auth.get_login_user();
			if (!user.my_projects.contains(project)) {
				// ユーザー情報に参加しているプロジェクトの情報を追加
				user.my_projects.add(project);
				user.save();
				// プロジェクト情報に参加している利用者の情報を追加
				project.project_member.add(user);
				project.save();
			}

			List<TaskInformation> タスク一覧 = TaskInformation.find("byProject",
					project).fetch();

			render(project_name, タスク一覧, user);
		}
	}

	/**
	 * タスク作成処理<br />
	 * 同じタスクを別の人も登録するかもしれないので，重複チェックはしない
	 * 
	 * @param project_name
	 *            プロジェクト名
	 * @param task_name
	 *            タスク名
	 */
	public static void taskCreate(String project_name, String task_name,
			Date deadline) {
		// プロジェクト情報を取得
		ProjectInformation project = ProjectInformation.find("byProject_name",
				project_name).first();

		// ユーザー情報を取得
		UserInformation user = Auth.get_login_user();
		
		//タスクがあることと日付が正しいかを確認する変数
		boolean is_task = true;
		boolean correct_deadline = true;
		
		//タスクがあるかどうか調べる
		if (task_name.length() == 0) {
			is_task = false;
		}
		
		// 現在の時刻と渡された時刻を比べる
		Date date = new Date();
		if (deadline != null && date.getTime() > deadline.getTime()) {
			correct_deadline = false;
		}
		
		//正しい入力がされていたらタスクを追加する
		if (is_task == true && correct_deadline == true) {
			// タスク作成
			TaskInformation task = new TaskInformation(project, user,
					task_name, deadline);
			task.save();

			// ユーザー情報に担当タスクを追加
			user.my_tasks.add(task);
			user.save();

			// プロジェクト情報にタスクを追加
			project.task_list.add(task);
			task.save();

			flash.put("success", "タスク[" + task_name + "]を作成しました");

			taskListScreen(project_name);
		}else{
			flash.put("error", "タスク名または締め切りを正しく入力してください");
			taskListScreen(project_name);
		}

	}

	/**
	 * タスク編集処理<br />
	 * 同じタスクを別の人も登録するかもしれないので，重複チェックはしない
	 * 
	 * @param project_name
	 *            プロジェクト名
	 * @param task_name
	 *            タスク名
	 */
	public static void taskEdit(String project_name, String task_name,
			Date deadline, Long id_of_task) {
		TaskInformation task = TaskInformation.findById(id_of_task);

		if (task == null) {
			// タスク情報が取得できない場合はid_of_taskがおかしいので，タスク一覧画面に戻す
			flash.put("error", "タスクを編集できませんでした");
			taskListScreen(project_name);
		} else {
			// タスクを編集する
			String previous_task = task.task_name;
			task.task_name = task_name;
			task.deadline = deadline;
			task.save();

			flash.put("success", "タスク[" + previous_task + "]をタスク["
					+ task.task_name + "]に編集しました");

			taskListScreen(project_name);
		}
	}

	/**
	 * 選択したタスクを削除状態にする<br />
	 * タスク名だと重複している可能性があるので，Taskテーブルのidを使う
	 * 
	 * @param project_name
	 * @param id_of_task
	 */
	public static void taskDelete(String project_name, Long id_of_task) {
		// プロジェクト情報を取得
		ProjectInformation project = ProjectInformation.find("byProject_name",
				project_name).first();
		// ユーザ情報を取得
		UserInformation user = Auth.get_login_user();
		// タスク情報を取得
		TaskInformation task = TaskInformation.findById(id_of_task);

		if (task == null) {
			// タスク情報が取得できない場合はid_of_taskがおかしいので，タスク一覧画面に戻す
			flash.put("error", "不明なタスクです");
			taskListScreen(project_name);
		} else {
			// タスクを削除する
			String previous_task = task.task_name;
			// プロジェクトの参加メンバーから削除
			project.task_list.remove(task);
			project.save();

			// ユーザの参加プロジェクトから削除
			user.my_tasks.remove(task);
			user.save();

			task.delete();

			flash.put("success", "タスク[" + previous_task + "]を削除しました");

			taskListScreen(project_name);
		}
	}

	/**
	 * 選択したタスクを完了状態にする<br />
	 * タスク名だと重複している可能性があるので，Taskテーブルのidを使う
	 * 
	 * @param project_name
	 * @param id_of_task
	 */
	public static void taskComplete(String project_name, Long id_of_task) {
		TaskInformation task = TaskInformation.findById(id_of_task);

		if (task == null) {
			// タスク情報が取得できない場合はid_of_taskがおかしいので，タスク一覧画面に戻す
			flash.put("error", "不明なタスクです");
			taskListScreen(project_name);
		} else {
			// タスクを完了状態にする
			task.is_done = true;
			task.save();

			flash.put("success", "タスク[" + task.task_name + "]を完了状態にしました");

			taskListScreen(project_name);
		}
	}

	/**
	 * プロジェクト脱退の確認画面を表示する. <br />
	 * 担当していて未完了のタスクがある場合は、タスク一覧画面に戻す
	 * 
	 * @param project_name
	 *            プロジェクト名
	 */
	public static void projectWithdrawalConfirmationScreen(String project_name) {
		// プロジェクト情報を取得
		ProjectInformation project = ProjectInformation.find("byProject_name",
				project_name).first();
		// ユーザ情報を取得
		UserInformation user = Auth.get_login_user();

		// 担当していて未完了のタスクを検索
		List<TaskInformation> task_list = TaskInformation.find(
				"byProjectAndAssign_userAndIs_done", project, user, false)
				.fetch();

		if (!task_list.isEmpty()) {
			flash.put("error", "完了していないタスクがあります");
			taskListScreen(project_name);
		} else {
			render(project_name);
		}
	}

	/**
	 * プロジェクトから脱退する
	 * 
	 * @param project_name
	 *            プロジェクト名
	 */
	public static void projectWithdrawal(String project_name) {
		// プロジェクト情報を取得
		ProjectInformation project = ProjectInformation.find("byProject_name",
				project_name).first();

		// ユーザ情報を取得
		UserInformation user = Auth.get_login_user();

		// プロジェクトの参加メンバーから削除
		project.project_member.remove(user);
		project.save();

		// ユーザの参加プロジェクトから削除
		user.my_projects.remove(project);
		user.save();

		flash.put("success", "プロジェクト「" + project_name + "」から脱退しました");

		projectListScreen();

	}
}