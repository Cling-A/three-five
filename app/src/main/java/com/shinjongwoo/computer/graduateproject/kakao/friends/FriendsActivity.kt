package com.shinjongwoo.computer.graduateproject.kakao.friends

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.kakao.friends.AppFriendContext
import com.kakao.friends.AppFriendOrder
import com.kakao.friends.response.AppFriendsResponse
import com.kakao.friends.response.model.AppFriendInfo
import com.kakao.kakaolink.v2.KakaoLinkResponse
import com.kakao.kakaolink.v2.KakaoLinkService
import com.kakao.kakaotalk.callback.TalkResponseCallback
import com.kakao.kakaotalk.response.KakaoTalkProfile
import com.kakao.kakaotalk.response.MessageSendResponse
import com.kakao.kakaotalk.v2.KakaoTalkService
import com.kakao.message.template.*
import com.kakao.network.ErrorResult
import com.kakao.network.callback.ResponseCallback
import com.kakao.usermgmt.UserManagement
import com.kakao.usermgmt.callback.MeV2ResponseCallback
import com.kakao.usermgmt.response.MeV2Response
import com.kakao.usermgmt.response.model.Profile
import com.kakao.util.OptionalBoolean
import kotlinx.android.synthetic.main.friend_activity.*
import com.shinjongwoo.computer.graduateproject.R


class FriendsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("abcd", "Friend Create Start")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.friend_activity)
        ProfileBtn.setOnClickListener { onClickProfile() };
        FriendsBtn.setOnClickListener { onClickFriend() };
        aBtn.setOnClickListener { a() };
        msgBtn1.setOnClickListener { sendMsg1() };
        msgBtn2.setOnClickListener { sendMsg2() };
        msgBtn3.setOnClickListener { sendMsg3() };
        Log.d("abcd", "Friend Create End")
    }

    private fun onClickProfile() {
        KakaoTalkService.getInstance()
            .requestProfile(object : TalkResponseCallback<KakaoTalkProfile?>() {
                override fun onNotKakaoTalkUser() {
                    Log.e("KAKAO_API", "카카오톡 사용자가 아님")
                }

                override fun onSessionClosed(errorResult: ErrorResult) {
                    Log.e("KAKAO_API", "세션이 닫혀 있음: $errorResult")
                }

                override fun onFailure(errorResult: ErrorResult) {
                    Log.e("KAKAO_API", "카카오톡 프로필 조회 실패: $errorResult")
                }

                override fun onSuccess(result: KakaoTalkProfile?) {
                    Log.i("KAKAO_API", "카카오톡 닉네임: " + result!!.nickName)
                    Log.i("KAKAO_API", "카카오톡 프로필이미지: " + result!!.profileImageUrl)
                }
            })
    }

    private fun onClickFriend() { // 조회 요청
        var context = AppFriendContext(AppFriendOrder.NICKNAME, 0, 100, "asc")

        KakaoTalkService.getInstance()
            .requestAppFriends(context, object : TalkResponseCallback<AppFriendsResponse?>() {
                override fun onNotKakaoTalkUser() {
                    Log.e("KAKAO_API", "카카오톡 사용자가 아님")
                }

                override fun onSessionClosed(errorResult: ErrorResult) {
                    Log.e("KAKAO_API", "세션이 닫혀 있음: $errorResult")
                }

                override fun onFailure(errorResult: ErrorResult) {
                    Log.e("KAKAO_API", "친구 조회 실패: $errorResult")
                }

                override fun onSuccess(result: AppFriendsResponse?) {
                        Log.i("KAKAO_API", "친구 조회 성공")
                        if (result!!.friends == null) {
                            Log.i("My TAP", "FRINED IS NULL")
                        } else {
                            Log.i("My TAP", "FRINED IS NULL")
                            var friendList: List<AppFriendInfo> = result.friends;
                            Log.i("My TAP", friendList.size.toString());
                        }

                        for (friend in result!!.friends) {
                            Log.d("KAKAO_API", friend.toString())
                            val uuid = friend.uuid // 메시지 전송 시 사용
                        }

                }
            })
    }

    private fun a() {
        UserManagement.getInstance()
            .me(object : MeV2ResponseCallback() {
                override fun onSessionClosed(errorResult: ErrorResult) {
                    Log.e("KAKAO_API", "세션이 닫혀 있음: $errorResult")
                }

                override fun onFailure(errorResult: ErrorResult) {
                    Log.e("KAKAO_API", "사용자 정보 요청 실패: $errorResult")
                }

                override fun onSuccess(result: MeV2Response) {
                    Log.i("KAKAO_API", "사용자 아이디: " + result.id)
                    val kakaoAccount = result.kakaoAccount
                    if (kakaoAccount != null) { // 이메일
                        val email = kakaoAccount.email
                        if (email != null) {
                            Log.i("KAKAO_API", "email: $email")
                        } else if (kakaoAccount.emailNeedsAgreement() == OptionalBoolean.TRUE) {
                            // 동의 요청 후 이메일 획득 가능
                            // 단, 선택 동의로 설정되어 있다면 서비스 이용 시나리오 상에서 반드시 필요한 경우에만 요청해야 합니다.
                        } else { // 이메일 획득 불가

                        }
                        // 프로필
                        val profile: Profile? = kakaoAccount.profile
                        if (profile != null) {
                            Log.d("KAKAO_API", "nickname: " + profile.getNickname())
                            Log.d(
                                "KAKAO_API",
                                "profile image: " + profile.getProfileImageUrl()
                            )
                            Log.d(
                                "KAKAO_API",
                                "thumbnail image: " + profile.getThumbnailImageUrl()
                            )
                        } else if (kakaoAccount.profileNeedsAgreement() == OptionalBoolean.TRUE) { // 동의 요청 후 프로필 정보 획득 가능
                        } else { // 프로필 획득 불가
                        }
                    }
                }
            })
    }

    private fun sendMsg1() {
        val params = makeFeedTemplateMessage();
        KakaoLinkService.getInstance()
            .sendDefault(this, params, object : ResponseCallback<KakaoLinkResponse>() {
                override fun onFailure(errorResult: ErrorResult) {
                    Log.e("KAKAO_API", "카카오링크 공유 실패: $errorResult")
                }

                override fun onSuccess(result: KakaoLinkResponse) {
                    Log.i("KAKAO_API", "카카오링크 공유 성공")
                    // 카카오링크 보내기에 성공했지만 아래 경고 메시지가 존재할 경우 일부 컨텐츠가 정상 동작하지 않을 수 있습니다.
                    Log.w("KAKAO_API", "warning messages: " + result.warningMsg)
                    Log.w("KAKAO_API", "argument messages: " + result.argumentMsg)
                }
            })
    }

    private fun sendMsg2() {
        val params = makeFeedTemplateMessage();
        KakaoTalkService.getInstance()
            .requestSendMemo(object : TalkResponseCallback<Boolean?>() {
                override fun onNotKakaoTalkUser() {
                    Log.e("KAKAO_API", "카카오톡 사용자가 아님")
                }

                override fun onSessionClosed(errorResult: ErrorResult) {
                    Log.e("KAKAO_API", "세션이 닫혀 있음: $errorResult")
                }

                override fun onFailure(errorResult: ErrorResult) {
                    Log.e("KAKAO_API", "나에게 보내기 실패: $errorResult")
                }

                override fun onSuccess(result: Boolean?) {
                    Log.i("KAKAO_API", "나에게 보내기 성공")
                }
            }, params)
    }

    fun sendMsg3(){
        var context = AppFriendContext(AppFriendOrder.NICKNAME, 0, 100, "asc")
        val params = makeFeedTemplateMessage();
        var uuids =  mutableListOf<String>();

        KakaoTalkService.getInstance()
            .requestAppFriends(context, object : TalkResponseCallback<AppFriendsResponse?>() {
                override fun onNotKakaoTalkUser() {
                    Log.e("KAKAO_API", "카카오톡 사용자가 아님")
                }

                override fun onSessionClosed(errorResult: ErrorResult) {
                    Log.e("KAKAO_API", "세션이 닫혀 있음: $errorResult")
                }

                override fun onFailure(errorResult: ErrorResult) {
                    Log.e("KAKAO_API", "친구 조회 실패: $errorResult")
                }

                override fun onSuccess(result: AppFriendsResponse?) {
                    Log.i("KAKAO_API", "친구 조회 성공")
                    for (friend in result!!.friends) {
                        Log.d("KAKAO_API", friend.toString())
                        uuids.add(friend.uuid)
                        Log.d("MY TAP", uuids.toString())
                    }

                    Log.d("AAA", uuids.size.toString())
                    Log.d("AAA", uuids.toString())

                    // 기본 템플릿으로 친구에게 보내기
                    // 기본 템플릿으로 친구에게 보내기
                    KakaoTalkService.getInstance()
                        .sendMessageToFriends(
                            uuids,
                            params,
                            object : TalkResponseCallback<MessageSendResponse?>() {
                                override fun onNotKakaoTalkUser() {
                                    Log.e("KAKAO_API", "카카오톡 사용자가 아님")
                                }

                                override fun onSessionClosed(errorResult: ErrorResult) {
                                    Log.e("KAKAO_API", "세션이 닫혀 있음: $errorResult")
                                }

                                override fun onFailure(errorResult: ErrorResult) {
                                    Log.e("KAKAO_API", "친구에게 보내기 실패: $errorResult")
                                }

                                override fun onSuccess(result: MessageSendResponse?) {
                                    if (result?.successfulReceiverUuids() != null) {
                                        Log.i("KAKAO_API", "친구에게 보내기 성공")
                                        Log.d(
                                            "KAKAO_API",
                                            "전송에 성공한 대상: " + result.successfulReceiverUuids()
                                        )
                                    }
                                    if (result?.failureInfo() != null) {
                                        Log.e("KAKAO_API", "일부 사용자에게 메시 보내기 실패")
                                        for (failureInfo in result?.failureInfo()!!) {
                                            Log.d("KAKAO_API", "code: " + failureInfo.code())
                                            Log.d("KAKAO_API", "msg: " + failureInfo.msg())
                                            Log.d(
                                                "KAKAO_API",
                                                "failure_uuids: " + failureInfo.receiverUuids()
                                            )
                                        }
                                    }
                                }
                            })
                }
            })

    }



    // 이하 private
    private fun makeFeedTemplateMessage() : TemplateParams{
        return FeedTemplate
            .newBuilder(
                ContentObject.newBuilder(
                    "디저트 사진",
                    "http://mud-kage.kakao.co.kr/dn/NTmhS/btqfEUdFAUf/FjKzkZsnoeE4o19klTOVI1/openlink_640x640s.jpg",
                    LinkObject.newBuilder()
                        .setWebUrl("https://developers.kakao.com")
                        .setMobileWebUrl("https://developers.kakao.com")
                        .build()
                )
                    .setDescrption("아메리카노, 빵, 케익")
                    .build()
            )
            .setSocial(
                SocialObject.newBuilder()
                    .setLikeCount(10)
                    .setCommentCount(20)
                    .setSharedCount(30)
                    .setViewCount(40)
                    .build()
            )
            .addButton(
                ButtonObject(
                    "웹에서 보기",
                    LinkObject.newBuilder()
                        .setWebUrl("https://developers.kakao.com")
                        .setMobileWebUrl("https://developers.kakao.com")
                        .build()
                )
            )
            .addButton(
                ButtonObject(
                    "앱에서 보기",
                    LinkObject.newBuilder()
                        .setAndroidExecutionParams("key1=value1")
                        .setIosExecutionParams("key1=value1")
                        .build()
                )
            )
            .build()
    }
}