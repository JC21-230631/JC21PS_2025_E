package jp.co.jc21ps.activity_management.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpSession;
import jp.co.jc21ps.activity_management.dto.JoinApprovalDataDto;
import jp.co.jc21ps.activity_management.dto.JoinApprovalDto;
import jp.co.jc21ps.activity_management.dto.SessionDto;
import jp.co.jc21ps.activity_management.form.JoinApprovalDataForm;
import jp.co.jc21ps.activity_management.form.JoinApprovalForm;
import jp.co.jc21ps.activity_management.service.CommonService;
import jp.co.jc21ps.activity_management.service.JoinApprovalService;
import jp.co.jc21ps.activity_management.dto.JoinApprovalNameDto;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/joinApproval")
public class JoinApprovalController {

    private final JoinApprovalService joinApprovalService;
    private final CommonService commonService;
    private final MessageSource messageSource;

    public JoinApprovalController(JoinApprovalService joinApprovalService, CommonService commonService,
            MessageSource messageSource) {

        this.joinApprovalService = joinApprovalService;
        this.commonService = commonService;
        this.messageSource = messageSource;
    }

    @GetMapping
    public ModelAndView getjoinApproval(HttpSession session) {

        ModelAndView mav = new ModelAndView();

        // セッションからuserId,clubIdを取得
        SessionDto sessionDto = commonService.getSessionDto(session);
        String userId = sessionDto.getUserId();
        String leaderClubId = sessionDto.getClubId();

        // セッションが切れた場合、エラー画面に遷移
        if (leaderClubId.isEmpty()) {
            mav.setViewName("error");
            return mav;
        }

        try {
            // セッションが切れた場合、エラー画面に遷移する
            if (userId.isEmpty()) {
                mav.setViewName("error");
                return mav;
            }

            // dtoに値をセット
            JoinApprovalDto joinApprovalDto = new JoinApprovalDto();
            joinApprovalDto.setUserId(userId);
            joinApprovalDto.setClubId(leaderClubId);

            JoinApprovalNameDto viewList = joinApprovalService.getJoinApprovalData(joinApprovalDto);
            List<JoinApprovalForm> responseForm = new ArrayList<>();

            // formに値をセット
            for (JoinApprovalDto dto : viewList.getJoinApprovalDto()) {

                JoinApprovalForm requestList = new JoinApprovalForm();
                requestList.setClubId(dto.getClubId());
                requestList.setUserId(dto.getUserId());
                requestList.setClubName(dto.getClubName());
                requestList.setUserName(dto.getUserName());

                // responseFormにリストを追加
                responseForm.add(requestList);

            }

            mav.addObject("clubName", viewList.getClubName());

            // messages.propertiesからメッセージを取得
            String resultMessage = messageSource.getMessage("notrequest", null, Locale.getDefault());

            // 部員登録申請がない場合のメッセージ
            mav.addObject("message", resultMessage);
            mav.addObject("joinApprovalform", responseForm);
            mav.addObject("leaderClubId", leaderClubId);
            mav.setViewName("JoinApproval");

        } catch (Exception e) {
            mav.addObject("leaderClubId", leaderClubId);
            mav.setViewName("error");
        }
        return mav;
    }

    // 否認
    @PostMapping("/denial")
    public ModelAndView denialRequest(JoinApprovalDataForm paramForm, HttpSession session) {

        // paramDtoに値をセット
        JoinApprovalDataDto paramDto = new JoinApprovalDataDto();
        paramDto.setUserId(paramForm.getUserId());
        paramDto.setClubId(paramForm.getClubId());
        paramDto.setLeaderFlg(paramForm.isLeaderFlg());

        // セッションからclubIdを取得
        SessionDto sessionDto = commonService.getSessionDto(session);
        String leaderClubId = sessionDto.getClubId();

        ModelAndView mav = new ModelAndView();

        // セッションが切れた場合、エラー画面に遷移
        if (leaderClubId.isEmpty()) {
            mav.setViewName("error");
            return mav;
        }

        try {
            // サービスからdeleteメソッドを呼び出す
            joinApprovalService.deleteRequestInfo(paramDto);

            // deleteに成功した場合、部員登録承認画面に遷移
            mav.addObject("leaderClubId", leaderClubId);
            mav.setViewName("redirect:/joinApproval");
        } catch (Exception e) {
            // deleteに失敗した場合、エラー画面に遷移
            mav.addObject("leaderClubId", leaderClubId);
            mav.setViewName("error");
        }

        return mav;

    }

    // 承認
    @PostMapping("/approval")
    public ModelAndView approvalRequest(JoinApprovalDataForm paramForm, HttpSession session) {
        // paramDtoに値をセット
        JoinApprovalDataDto paramDto = new JoinApprovalDataDto();
        paramDto.setUserId(paramForm.getUserId());
        paramDto.setClubId(paramForm.getClubId());
        paramDto.setLeaderFlg(paramForm.isLeaderFlg());

        // セッションからclubIdを取得
        SessionDto sessionDto = commonService.getSessionDto(session);
        String leaderClubId = sessionDto.getClubId();

        ModelAndView mav = new ModelAndView();

        // セッションが切れた場合、エラー画面に遷移
        if (leaderClubId == null) {
            mav.setViewName("error");
            return mav;
        }

        try {
            // 部員テーブルにinsert
            joinApprovalService.insertRequestInfo(paramDto);
            // 申請テーブルからdelete（申請画面に承認したユーザーが残らないようにするため）
            joinApprovalService.deleteRequestInfo(paramDto);

            // insert, deleteに成功した場合、部員登録承認画面に遷移
            mav.addObject("leaderClubId", leaderClubId);
            mav.setViewName("redirect:/joinApproval");

        } catch (Exception e) {
            // insert, deleteに失敗した場合、エラー画面に遷移
            mav.addObject("leaderClubId", leaderClubId);
            mav.setViewName("error");
        }
        return mav;
    }

}
