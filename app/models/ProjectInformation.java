package models;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import play.db.jpa.Model;

/**
 * プロジェクト情報エンティティ
 * @author maruyama
 */
@Entity
public class ProjectInformation extends Model {
    /**
     * プロジェクト名
     */
    public String project_name;
    
    /**
     * このプロジェクトの持っているタスク一覧
     */
    @OneToMany
    public List<TaskInformation> task_list = new ArrayList<TaskInformation>();
    
    /**
     * このプロジェクトに参加しているユーザー一覧
     */
    @ManyToMany
    public List<UserInformation> project_member = new ArrayList<UserInformation>();
    
    /**
     * プロジェクト情報のコンストラクタ<br />
     * 作成日とかも特に記録しないので，プロジェクト名のみ
     * @param project_name プロジェクト名
     */
    public ProjectInformation(String project_name){
        this.project_name = project_name;
    }
}
