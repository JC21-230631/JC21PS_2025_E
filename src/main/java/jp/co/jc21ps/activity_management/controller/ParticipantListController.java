package jp.co.jc21ps.activity_management.controller;
 
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.context.MessageSource;
import jp.co.jc21ps.activity_management.dto.ParticipantListDto;
import jp.co.jc21ps.activity_management.form.ParticipantListForm;
import jp.co.jc21ps.activity_management.service.CommonService;
import jp.co.jc21ps.activity_management.service.ParticipantListService;
import jp.co.jc21ps.activity_management.dto.ParticipantDto;
import jp.co.jc21ps.activity_management.dto.SessionDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpSession;
 
@Controller
@RequestMapping("/participantList")
public class ParticipantListController {
 
    private final ParticipantListService participantListService;
    private final CommonService commonService;
    private final MessageSource messageSource;
 
    public ParticipantListController(ParticipantListService participantListService, CommonService commonService,
            MessageSource messageSource) {
        this.participantListService = participantListService;
        this.commonService = commonService;
        this.messageSource = messageSource;
    }
 
    @GetMapping
    public ModelAndView dispParticipantList(@RequestParam(value = "activityId", required = true) String activityId,
            HttpSession session) {
 
        ModelAndView mav = new ModelAndView();
 
        // 活動IDが存在しない場合、エラー画面に遷移
        if (activityId.isEmpty()) {
            mav.setViewName("error");
            return mav;
        }
 
        /*
         * TODO ➊ セッションからuserId, clubIdを取得
         */
        SessionDto sessionDto = commonService.getSessionDto(session);
        String userId = sessionDto.getUserId();
        String clubId = sessionDto.getClubId();

        // セッションが切れた場合、エラー画面に遷移
        if (userId == null || userId.isEmpty()) {
            mav.setViewName("error");
            return mav;
        }

        /*
         * ➋TODO dtoに値をセット
         */
        ParticipantListDto paramDto = new ParticipantListDto();
        paramDto.setActivityId(activityId);
        paramDto.setUserId(userId);

        try {
            /*
             * ➌TODO participantListServiceのgetParticipantListDataメソッドを呼び出す。
             */
            ParticipantDto participantDto = participantListService.getParticipantListData(paramDto);


            // 返却用のリスト
            List<ParticipantListForm> responseListForm = new ArrayList<>();
 
            /*
             * ➍ TODO responseListFormに値をセット
             */
            if (participantDto.getPariticipantListDto() != null) {
                for (ParticipantListDto dto : participantDto.getPariticipantListDto()) {
                    ParticipantListForm form = new ParticipantListForm();
                    form.setActivityId(dto.getActivityId());
                    form.setUserId(dto.getUserId());
                    form.setActivityName(dto.getActivityName());
                    form.setUserName(dto.getUserName());
                    responseListForm.add(form);
                }
            }

            /*
             * ➎ TODO 取得したデータを画面側に渡す。
             */
            mav.addObject("participantList", responseListForm);
            mav.addObject("activityName", participantDto.getActivityName());

            // messages.propertiesからメッセージを取得
            String resultMessage = messageSource.getMessage("notpariticipant", null, Locale.getDefault());
            mav.addObject("message", resultMessage);
            mav.addObject("clubId", clubId);

            // 遷移先の設定
            mav.setViewName("participantList");
        } catch (Exception e) {
            mav.setViewName("error");
        }
 
        return mav;
 
    }
 
}
 