package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.db.jpa.Model;

/**
 * タスク情報エンティティ
 * 
 * @author maruyama
 */
@Entity
public class TaskInformation extends Model {

	/**
	 * このタスクを持つプロジェクト
	 */
	@ManyToOne
	public ProjectInformation project;

	/**
	 * このタスクの担当者<br />
	 * 本サンプルコードでは，タスクの作成者＝担当者
	 */
	@ManyToOne
	public UserInformation assign_user;

	/**
	 * タスク名
	 */
	public String task_name;

	/**
	 * 完了フラグ．falseなら未完了，trueなら完了
	 */
	public Boolean is_done = false;
	
	/**
	 * 締め切り
	 */
	public Date deadline;
	
	/**
	 * タスクが持つタグ
	 */
	@OneToMany
	public List<TagInformation> my_tag = new ArrayList<TagInformation>();

	
	/**
	 * タスク情報のコンストラクタ
	 * 
	 * @param project
	 *            このタスクを持つプロジェクト
	 * @param assign_user
	 *            このタスクの担当者
	 * @param task_name
	 *            タスク名
	 * @param deadline
	 * 			  締め切り
	 */
	public TaskInformation(ProjectInformation project, UserInformation assign_user, String task_name,
			Date deadline) {
		this.task_name = task_name;
		this.project = project;
		this.assign_user = assign_user;
		this.deadline = deadline;
	}

}
