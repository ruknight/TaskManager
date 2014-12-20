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
    public static void adminScreen() {
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
    public static void userRegistrate(String user_id, String password) {

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

        adminScreen();
    }

    /**
     * プロジェクト作成処理<br />
     * 同じプロジェクト名は登録できない<br />
     * 処理の成功，失敗にかかわらず，管理者用TOP画面に戻す
     *
     * @param project_name プロジェクト名
     */
    public static void projectCreate(String project_name) {
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

        adminScreen();
    }

    /**
     * プロジェクト削除処理 プロジェクト削除前にプロジェクトのユーザ、タスクの情報も合わせて削除 処理後、管理者用TOP画面に戻す
     *
     * @param project_id ID
     */
    public static void projectDelete(Long project_id) {

        // プロジェクト情報取得        
        ProjectInformation project = ProjectInformation.findById(project_id);
        // プロジェクトのタスク情報をリストで取得
        List<TaskInformation> taskList = TaskInformation.find("byProject", project).fetch();
        // プロジェクトに参加しているユーザ情報を userList に格納
        List<UserInformation> userList = project.project_member;
        // タグ情報取得
        List<TagInformation> tagList = TagInformation.findAll();

        // ユーザのタスク、プロジェクト情報を削除
        for (UserInformation u : userList) {
            u.my_tasks.removeAll(taskList);
            u.my_projects.remove(project);
            // 変更内容を保存
            u.save();
        }

        // TaskInformation のタグを削除
        for (TaskInformation t : taskList) {
            t.my_tag.clear();
            t.save();
        }

        // TagInformation のタグを削除
        for (TagInformation tag : tagList) {
            // プロジェクトのタスク、かつタグ付きのものを削除
            if (project.task_list.contains(tag.task)) {
                tag.delete();
            }
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
         adminScreen();
    }

    /**
     * ここから、プロジェクト編集画面の処理
     */
    /**
     * プロジェクト編集画面 とりあえずテスト
     *
     * @param project_name
     */
    public static void projectEditScreen(String project_name) {
        List<UserInformation> ユーザー一覧 = UserInformation.findAll();
        List<ProjectInformation> プロジェクト一覧 = ProjectInformation.findAll();

        // プロジェクト情報取得
        ProjectInformation project = ProjectInformation.find("byProject_name", project_name).first();

        // プロジェクトに参加しているユーザ情報を userList に格納
        List<UserInformation> userList = project.project_member;

        render(userList, project);
    }

    /**
     * 管理者がユーザIDを入力してプロジェクトに利用者を追加する その前にユーザIDがシステムに登録されているか
     * すでにプロジェクトに参加していないかをチェックする
     *
     * @param user_id
     * @param project_name
     */
    public static void userAdd(String project_name, String user_id) {

        // プロジェクト情報を取得
        ProjectInformation project = ProjectInformation.find("byProject_name", project_name).first();
        // 追加するユーザIDがシステムに利用者として登録されているかチェック
        UserInformation user = UserInformation.find("byUser_id", user_id).first();
        if (user == null) {
            // user_idが存在しない
            flash.put("error", "利用者[" + user_id + "]はシステムに登録されていません");
            projectEditScreen(project_name);
        }

        // 追加するユーザIDがプロジェクトに参加しているかチェック
        if (user.my_projects.contains(project)) {
            flash.put("error", "利用者[" + user_id + "]はプロジェクトに参加しています");
            projectEditScreen(project_name);
        }

        // ユーザー情報に参加しているプロジェクトの情報を追加
        user.my_projects.add(project);
        user.save();
        // プロジェクト情報に参加している利用者の情報を追加
        project.project_member.add(user);
        project.save();

        // 成功表示
        flash.put("success", "利用者[" + user_id + "]を追加しました");

        projectEditScreen(project_name);
    }

    public static void userDelete(String user_id, String project_name) {
        // プロジェクト情報取得
        ProjectInformation project = ProjectInformation.find("byProject_name", project_name).first();
        // プロジェクトのタスク情報をリストで取得
        List<TaskInformation> taskList = TaskInformation.find("byProject", project).fetch();
        // ユーザ情報取得
        UserInformation user = UserInformation.find("byUser_id", user_id).first();
        // タグ情報取得
        List<TagInformation> tagList = TagInformation.findAll();
        
        // 参加メンバ情報、タスク情報の削除
        project.project_member.remove(user);
        project.task_list.removeAll(user.my_tasks);
        project.save();

        //taskList.removeAll(user.my_tasks);
        // ユーザのタスク、プロジェクト情報を削除
        user.my_projects.remove(project);
        user.my_tasks.removeAll(taskList);
        user.save();

        /**
         * 担当タスクのタグ情報削除 TaskInforamtion のタグ情報を削除してから TagInforamtion のタグ情報を削除する
         * そうしないとエラーが出て削除できない
         */
        for (TaskInformation t : taskList) {
            // 削除するユーザのタスクから削除する
            if (t.assign_user == user) {
                // ユーザのタスクタグ情報をすべて削除
                t.my_tag.clear();
                // 変更内容を保存
                t.save();
            }
        }
        for (TagInformation tag : tagList) {
            // 削除するユーザのタスクから削除する
            if (tag.task.assign_user == user) {
                tag.delete();
            }
        }
        // 担当タスクの削除
        for (TaskInformation t : taskList) {
            if (t.assign_user == user) {
                t.delete();
            }
        }

        // 成功表示
        flash.put("success", "利用者[" + user_id + "]を削除しました");

        projectEditScreen(project_name);
    }

}
