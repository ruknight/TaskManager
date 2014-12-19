package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.db.jpa.Model;
@Entity
public class TagInformation extends Model{

	/**
	 * タグ名
	 */
	public String tag_name;
	
	/**
	 * このタグをもつタスク
	 */
	@ManyToOne
	public TaskInformation task;
	
	/**
	 * タグ情報のコンストラクタ
	 *
	 * @param task
	 *            このタグをもつタスク
	 * @param tag_name
	 * 			  タグ名
	 */
	public TagInformation(TaskInformation task, String tag_name){
		this.task = task;
		this.tag_name = tag_name;
	}
}
