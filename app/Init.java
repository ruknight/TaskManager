import models.UserInformation;
import play.jobs.*;

/**
 * 初回起動時の処理<br />
 * 1. 管理者ユーザーの作成
 * @author maruyama
 */
@OnApplicationStart
public class Init extends Job{
    
    @Override
    public void doJob() {
        // ユーザーテーブルが空だったら，管理者ユーザーを作成する
        if(UserInformation.count() == 0){
            UserInformation user = new UserInformation("admin", "admin");
            user.is_admin = true;   // 管理者フラグをtrueにする
            user.save();
        }
    }
}
