package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;

/**
 * 管理者用処理
 *
 * @author maruyama
 */
@Check("AdminOnly")
@With(Secure.class)
public class Admin extends Controller {

    /**
     * 管理者用TOP画面<br />
     * ユーザー一覧とプロジェクト一覧を表示する
     */
    public static void 管理者用TOP画面() {
        List<UserInformation> ユーザー一覧 = UserInformation.findAll();
        List<ProjectInformation> プロジェクト一覧 = ProjectInformation.findAll();

        render(ユーザー一覧, プロジェクト一覧);
    }

    /**
     * 利用者登録処理<br />
     * 管理者がユーザーIDとパスワードを登録する<br />
     * 利用者は初めてログインするとパスワード変更を求められる<br />
     * 処理が終わったら管理者用TOP画面に戻す
     *
     * @param user_id ユーザーID
     * @param password パスワード
     */
    public static void 利用者登録処理(String user_id, String password) {

        // 重複チェック
        UserInformation user = UserInformation.find("byUser_id", user_id).first();
        if (user != null) {
            // user_idで検索HITしたので，既に存在する
            flash.put("error", "利用者[" + user_id + "]は既に存在します");
        } else {
            // 新規利用者登録
            user = new UserInformation(user_id, password);
            user.save();
            flash.put("success", "利用者[" + user_id + "]を登録しました");
        }

        管理者用TOP画面();
    }

    /**
     * プロジェクト作成処理<br />
     * 同じプロジェクト名は登録できない<br />
     * 処理の成功，失敗にかかわらず，管理者用TOP画面に戻す
     *
     * @param project_name プロジェクト名
     */
    public static void プロジェクト作成処理(String project_name) {
        // 重複チェック
        ProjectInformation project = ProjectInformation.find("byProject_name", project_name).first();
        if (project != null) {
            // 既に存在する
            flash.put("error", "プロジェクト[" + project_name + project.project_member + "]は既に存在します");
        } else {
            // 新規プロジェクト作成
            project = new ProjectInformation(project_name);
            project.save();
            flash.put("success", "プロジェクト[" + project_name + "]を作成しました");
        }

        管理者用TOP画面();
    }

    /**
     * プロジェクト削除処理 プロジェクト削除前にプロジェクトのユーザ、タスクの情報も合わせて削除 処理後、管理者用TOP画面に戻す
     *
     * @param project_name プロジェクト名
     */
    public static void プロジェクト削除処理(String project_name) {

        // プロジェクト情報取得
        ProjectInformation project = ProjectInformation.find("byProject_name", project_name).first();
        // プロジェクトのタスク情報をリストで取得
        List<TaskInformation> taskList = TaskInformation.find("byProject", project).fetch();
        // プロジェクトに参加しているユーザ情報を userList に格納
        List<UserInformation> userList = project.project_member;

        /**
         * プロジェクト削除前に、プロジェクト内のタスク、ユーザ情報を削除 かつ、プロジェクトのタスクリスト、メンバリストを削除
         * その後、タスク、プロジェクトを削除する
         */
        // ユーザのタスク、プロジェクト情報を削除
        for (UserInformation u : userList) {
            u.my_tasks.removeAll(taskList);
            u.my_projects.remove(project);
            // 変更内容を保存
            u.save();
        }

        // プロジェクトの参加メンバ情報削除
        project.project_member.removeAll(userList);
        // タスクリスト削除
        project.task_list.removeAll(taskList);
        // 変更内容を保存
        project.save();

        // プロジェクト内のタスクをすべて削除
        for (TaskInformation t : taskList) {
            t.delete();
        }
        // プロジェクト削除
        project.delete();

        // 削除処理後は管理者トップ画面に戻る
        管理者用TOP画面();
    }
}
