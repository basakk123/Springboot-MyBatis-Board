package site.metacoding.red.web;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import lombok.RequiredArgsConstructor;
import site.metacoding.red.domain.boards.Boards;
import site.metacoding.red.domain.boards.BoardsDao;
import site.metacoding.red.domain.users.Users;
import site.metacoding.red.web.dto.request.boards.UpdateDto;
import site.metacoding.red.web.dto.request.boards.WriteDto;
import site.metacoding.red.web.dto.response.boards.MainDto;
import site.metacoding.red.web.dto.response.boards.PagingDto;

@RequiredArgsConstructor
@Controller
public class BoardsController {

	private final HttpSession session;
	private final BoardsDao boardsDao;

	// @PostMapping("/boards/{id}/delete")
	// @PostMapping("/boards/{id}/update")

	@PostMapping("/boards/{id}/update")
	public String update(@PathVariable Integer id, UpdateDto updateDto) {
		// 1. 영속화
		Boards boardsPS = boardsDao.findById(id);
		Users principal = (Users) session.getAttribute("principal");

		// 비정상 요청 체크
		if (boardsPS == null) { // if는 비정상 로직을 타게 해서 걸러내는 필터 역할을 하는게 좋다.
			return "errors/badPage";
		}

		// 인증 체크
		if (principal == null) {
			return "redirect:/loginForm";
		}

		// 권한 체크 ( 세션 principal.getId() 와 boardsPS의 userId를 비교)
		if (principal.getId() != boardsPS.getUsersId()) {
			return "errors/badPage";
		}

		// 2. 변경
		boardsPS.글수정(updateDto);

		// 3. 수행
		boardsDao.update(boardsPS);

		return "redirect:/boards/" + id;
	}

	@GetMapping("/boards/{id}/updateForm")
	public String updateForm(@PathVariable Integer id, Model model) {
		Boards boardsPS = boardsDao.findById(id);
		Users principal = (Users) session.getAttribute("principal");

		// 비정상 요청 체크
		if (boardsPS == null) { // if는 비정상 로직을 타게 해서 걸러내는 필터 역할을 하는게 좋다.
			return "errors/badPage";
		}

		// 인증 체크
		if (principal == null) {
			return "redirect:/loginForm";
		}

		// 권한 체크 ( 세션 principal.getId() 와 boardsPS의 userId를 비교)
		if (principal.getId() != boardsPS.getUsersId()) {
			return "errors/badPage";
		}

		model.addAttribute("boards", boardsPS);
		return "boards/updateForm";
	}

	@PostMapping("/boards/{id}/delete")
	public String deleteBoards(@PathVariable Integer id) {
		Users principal = (Users) session.getAttribute("principal");
		Boards boardsPS = boardsDao.findById(id);

		// 비정상 요청 체크
		if (boardsPS == null) { // if는 비정상 로직을 타게 해서 걸러내는 필터 역할을 하는게 좋다.
			return "errors/badPage";
		}

		// 인증 체크
		if (principal == null) {
			return "redirect:/loginForm";
		}

		// 권한 체크 ( 세션 principal.getId() 와 boardsPS의 userId를 비교)
		if (principal.getId() != boardsPS.getUsersId()) {
			return "errors/badPage";
		}

		boardsDao.delete(id); // 핵심 로직
		return "redirect:/";
	}

	@PostMapping("/boards")
	public String writeBoards(WriteDto writeDto) {
		Users principal = (Users) session.getAttribute("principal");

		if (principal == null) { // 인증코드
			return "redirect:/loginForm";
		}

		boardsDao.insert(writeDto.toEntity(principal.getId()));
		return "redirect:/";
	}

	// http://localhost:8000/
	// http://localhost:8000/?page=0 or 1 or 2
	// 1번째 ?page=0&keyword=스프링 (프라이머리 키 아니니까 쿼리스트링)
	@GetMapping({ "/", "/boards" })
	public String getBoardsList(Model model, Integer page, String keyword) { // 0->0, 1->10, 2->20
		if (page == null) {
			page = 0;
		}

		int startNum = page * 3;

		if (keyword == null || keyword.isEmpty()) {
			List<MainDto> boardsList = boardsDao.findAll(startNum);
			PagingDto paging = boardsDao.paging(page, null);

			paging.makeBlockInfo(keyword);

			model.addAttribute("paging", paging);
			model.addAttribute("boardsList", boardsList);

		} else {
			List<MainDto> boardsList = boardsDao.findSearch(startNum, keyword);
			PagingDto paging = boardsDao.paging(page, keyword);

			paging.makeBlockInfo(keyword);

			model.addAttribute("paging", paging);
			model.addAttribute("boardsList", boardsList);

		}
		return "boards/main";

	}

	@GetMapping("/boards/{id}")
	public String getBoardDetail(@PathVariable Integer id, Model model) {
		model.addAttribute("boards", boardsDao.findById(id));
		return "boards/detail";
	}

	@GetMapping("/boards/writeForm")
	public String writeForm() {
		Users principal = (Users) session.getAttribute("principal"); // 인증 검사, 권한은 필요없음
		if (principal == null) { // 인증코드
			return "redirect:/loginForm";
		}
		return "boards/writeForm"; // 부가코드
	}
}
