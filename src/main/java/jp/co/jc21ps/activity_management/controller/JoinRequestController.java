package jp.co.jc21ps.activity_management.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import jp.co.jc21ps.activity_management.dto.JoinRequestDto;
import jp.co.jc21ps.activity_management.dto.JoinRequestSaveDto;
import jp.co.jc21ps.activity_management.dto.SessionDto;
import jp.co.jc21ps.activity_management.form.JoinRequestSaveForm;
import jp.co.jc21ps.activity_management.service.CommonService;
import jp.co.jc21ps.activity_management.service.JoinRequestService;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/joinRequest")

public class JoinRequestController {

    private final JoinRequestService joinRequestService;
    private final MessageSource messageSource;
    private final CommonService commonService;

    // サービスをセット
    public JoinRequestController(JoinRequestService joinRequestService, MessageSource messageSource,
            CommonService commonService) {
        this.joinRequestService = joinRequestService;
        this.messageSource = messageSource;
        this.commonService = commonService;
    }

    @GetMapping
    public ModelAndView getJoinRequestById(HttpSession session, JoinRequestSaveForm paramForm,
            @ModelAttribute("joinOkMessage") String joinOkMessage) {

        ModelAndView mav = new ModelAndView();

        // セッションからuserIdを取得
        SessionDto sessionDto = commonService.getSessionDto(session);
        String userId = sessionDto.getUserId();
        String leaderClubId = sessionDto.getClubId();

        // セッションが切れた場合、エラー画面に遷移
        if (userId.isEmpty()) {
            mav.setViewName("error");
            return mav;
        }

        // formに値をセット
        JoinRequestSaveForm form = new JoinRequestSaveForm();
        form.setUserId(userId);

        // dtoに値をセット
        JoinRequestDto joinRequestDto = new JoinRequestDto();
        joinRequestDto.setUserId(userId);

        List<JoinRequestDto> joinRequestList = joinRequestService.findRequest(joinRequestDto);
        List<JoinRequestSaveForm> responseForm = new ArrayList<>();

        // formに値をセット
        for (JoinRequestDto dto : joinRequestList) {

            JoinRequestSaveForm saveData = new JoinRequestSaveForm();
            saveData.setClubName(dto.getClubName());
            saveData.setClubDescription(dto.getClubDescription());
            saveData.setClubId(dto.getClubId());

            // responseFormにリストを追加
            responseForm.add(saveData);

        }
        // リダイレクトされてきた登録申請成功のメッセージを、paramFormにセットする
        paramForm.setMessage(joinOkMessage);

        /*
         * TODO ➊ 初期表示情報取得結果に応じて、以下の条件文を完成させる。
         */
        // ① レスポンスが存在しない場合
        if (responseForm.isEmpty()) {
            // メッセージプロパティから notRequestClubMessage を取得する
            String notRequestClubMessage = messageSource.getMessage("notRequestClubMessage", null, Locale.getDefault());
            // 取得したメッセージをオブジェクトに追加する
            mav.addObject("notRequestClubMessage", notRequestClubMessage);
            // formから取得したメッセージをオブジェクトに追加する
            mav.addObject("joinRequestCompleteMessage", paramForm.getMessage());
        } else {
            // ② それ以外(=レスポンスが存在する場合)
            // レスポンスをオブジェクトに追加
            mav.addObject("responseForm", responseForm);
            // formから取得したメッセージをオブジェクトに追加する
            mav.addObject("joinRequestCompleteMessage", paramForm.getMessage());
        }

        mav.addObject("leaderClubId", leaderClubId);

        // 部員登録申請画面に遷移
        mav.setViewName("joinRequest");
        return mav;

    }

    // インサート処理
    @PostMapping("/save")
    public ModelAndView insertRequestClub(HttpSession session, JoinRequestSaveForm paramForm,
            RedirectAttributes redirectAttributes) {

        ModelAndView mav = new ModelAndView();

        // セッションからuserIdを取得
        SessionDto sessionDto = commonService.getSessionDto(session);
        String userId = sessionDto.getUserId();

        // セッションが切れた場合、エラー画面に遷移
        if (userId.isEmpty()) {
            mav.setViewName("error");
            return mav;
        }

        // dtoに値をセット
        JoinRequestSaveDto joinRequestSaveDto = new JoinRequestSaveDto();
        joinRequestSaveDto.setUserId(userId);
        joinRequestSaveDto.setClubId(paramForm.getClubId());

        try {
            boolean result = joinRequestService.insertJoinRequest(joinRequestSaveDto);
            /*
             * TODO ➋ インサートの成功、失敗に応じて、処理を変更する。
             */
            // ① 登録処理の戻り値がTrueの場合
            if (result) {
                // メッセージプロパティから joinRequestCompleteMessage を取得する
                String joinRequestCompleteMessage = messageSource.getMessage("joinRequestCompleteMessage", null, Locale.getDefault());
                // 取得したメッセージをformにセットする
                paramForm.setMessage(joinRequestCompleteMessage);
                // formから取得したメッセージをオブジェクトに追加する
                redirectAttributes.addFlashAttribute("joinOkMessage", joinRequestCompleteMessage);
                // /joinRequest にリダイレクトする
                mav.setViewName("redirect:/joinRequest");
            } else {
                // ② それ以外(=登録処理の戻り値がFalseの場合)
                // エラー画面に遷移する
                mav.setViewName("error");
            }

        } catch (Exception e) {
            mav.setViewName("error");
        }
        return mav;
    }
}
