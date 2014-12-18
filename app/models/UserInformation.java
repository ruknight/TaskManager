package models;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import play.db.jpa.Model;

/**
 * ユーザー情報エンティティ
 * @author maruyama
 */
@Entity
public class UserInformation extends Model {
    /**
     * ユーザーID
     */
    public String user_id;
    
    /**
     * パスワード（暗号化済み）
     */
    public String password;
    
    /**
     * 初回ログインフラグ．パスワードの変更を要求するために使う．<br />
     * trueならまだログインをしていない（＝パスワードを変更していない）
     */
    public Boolean is_first_login = true;
    
    /**
     * 削除フラグ．削除済みならtrue
     */
    public Boolean is_delete = false;
    
    /**
     * 管理者フラグ．管理者ならtrue
     */
    public Boolean is_admin = false;
    
    /**
     * このユーザの持っているタスク一覧
     */
    @OneToMany
    public List<TaskInformation> my_tasks = new ArrayList<TaskInformation>();
    
    /**
     * このユーザが参加しているプロジェクト一覧
     */
    @ManyToMany
    public List<ProjectInformation> my_projects = new ArrayList<ProjectInformation>();
    
    /**
     * ユーザー情報の新規作成
     * @param user_id ユーザーID
     * @param password パスワード
     */
    public UserInformation(String user_id, String password){
        this.user_id = user_id;
        this.password = UserInformation.get_password_hash(password);
        this.is_delete = false;
        this.is_admin = false;
    }
    
    /**
     * 指定したプロジェクトの自分が担当しているタスクの完了率<br />
     * ゼロ個の場合は，0.0を返す
     * @param project プロジェクト情報
     * @return 完了率
     */
    public Integer タスク完了率(ProjectInformation project){
        Integer 担当タスク数 = 担当タスク数(project);
        Integer 担当タスクの完了数 = 担当の完了タスク数(project);
        
        // 担当タスク数が0だと割れない
        if(担当タスク数 > 0){
            Long タスク完了率 = Math.round((double)担当タスクの完了数 / (double)担当タスク数 * 100);    // パーセンテージを求める
            
            return タスク完了率.intValue();
        } else {
            return 0;
        }
    }
    
    /**
     * 指定したプロジェクトの自分が担当しているタスク数
     * @param project プロジェクト情報
     * @return タスク数
     */
    public Integer 担当タスク数(ProjectInformation project){
        List<TaskInformation> task_list = TaskInformation.find("byProjectAndAssign_user", project, this).fetch();
        
        return task_list.size();
    }
    
    /**
     * 指定したプロジェクトの自分が担当しているタスクのうち，完了したタスク数
     * @param project プロジェクト情報
     * @return 完了済みタスク数
     */
    public Integer 担当の完了タスク数(ProjectInformation project){
        List<TaskInformation> task_list = TaskInformation.find("byProjectAndAssign_userAndIs_done", project, this, true).fetch();
        return task_list.size();
    }
    
    /**
     * パスワードの暗号化<br />
     * レインボーハッシュまでいかないが２回暗号化することで辞書攻撃を回避
     * @param password 暗号化前のパスワード
     * @return 暗号化済みの文字列
     */
    public static String get_password_hash(String password){
        password = play.libs.Crypto.passwordHash(password + "task");
        password = play.libs.Crypto.passwordHash("manager" + password);
        return password;
    }
}
