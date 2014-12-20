package controllers;

import models.UserInformation;

/**
 * Play framework組み込みのログイン制御クラス
 * @author maruyama
 */
public class Auth extends Secure.Security {

    /**
     * 組み込みの認証メソッド．ログイン成功ならtrue，失敗ならfalse<br />
     * 引数のusernameとpasswordは変更できない
     * @param username ユーザー名
     * @param password パスワード
     * @return 認証成功ならtrue，認証失敗ならfalse
     */
    static boolean authentify(String username, String password) {
        // パスワードを暗号化する
        password = UserInformation.get_password_hash(password);
        
        // ユーザーIDとパスワードで検索する
        UserInformation user = UserInformation.find("byUser_idAndPassword", username, password).first();
        
        if(user == null){
            // 検索でヒットしなかったので，認証失敗
            return false;
        } else {
            // 検索でヒットしたので，認証は成功
            
            if(user.is_delete == true){
                return false;   // 削除フラグが立っているので，ログイン失敗
            } else {
                return true;
            }
        }
        
    }

    /**
     * 組み込みの権限チェック用メソッド．権限があるならtrueを返す．<br />
     * ここでは，管理者かどうかチェックする
     * @param profile 権限の名称
     * @return 権限があるならtrue，権限がないならfalse
     */
    static boolean check(String profile) {
        
        // ログイン中のユーザー情報を取得
        UserInformation user = get_login_user();
        
        
        if(user != null){           // 一応，nullチェック
            if(profile.equals("AdminOnly")){    // @Check("AdminOnly")だったら管理者専用機能
                return user.is_admin;   // 管理者にはtrueが入っているはず
            }
            // 利用者用画面（@Check("MemberOnly")）は管理者はアクセスできない
            if(profile.equals("MemberOnly")){
                return !user.is_admin;
            }
        }
        return false;   // 上記のどれでもなかったら，アクセス拒否
    }
    
    /**
     * 認証成功時の処理（Play framework組み込みの機能）<br />
     * ここでは，管理者と利用者のトップ画面を変えるために使う
     */
    static void onAuthenticated(){
        // ログイン中のユーザー情報を取得
        UserInformation user = get_login_user();
        
        if(user.is_admin){
            // 管理者なら管理者用TOP画面
            Admin.adminScreen();
        } else {
            // 管理者でなければ，プロジェクト一覧画面
            TaskManager.projectListScreen();
        }
    }
    
    /**
     * ログイン中のユーザーを取得する
     * @return ユーザー情報
     */
    public static UserInformation get_login_user(){
        UserInformation user = UserInformation.find("byUser_id", Auth.connected()).first();
        return user;
    }
}
