package site.metacoding.red.web;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import lombok.RequiredArgsConstructor;
import oracle.jdbc.proxy.annotation.Post;
import site.metacoding.red.domain.boards.Boards;
import site.metacoding.red.domain.boards.BoardsDao;
import site.metacoding.red.domain.users.Users;
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

	@PostMapping("/boards/{id}/delete")
	public String deleteBoards(@PathVariable Integer id) {
		Users principal = (Users) session.getAttribute("principal");
		Boards boardsPS = boardsDao.findById(id);
		
		// 비정상 요청 체크
		if (boardsPS == null) { // if는 비정상 로직을 타게 해서 걸러내는 필터 역할을 하는게 좋다.
			return "redirect:/boards/" + id;
		}

		// 인증 체크
		if (principal == null) {
			return "redirect:/loginForm";
		}

		// 권한 체크 ( 세션 principal.getId() 와 boardsPS의 userId를 비교)
		if (principal.getId() != boardsPS.getUsersId()) {
			return "redirect:/boards/" + id;
		}
	
		boardsDao.delete(id);
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
	@GetMapping({ "/", "/boards" })
	public String getBoardsList(Model model, Integer page) { // 0->0, 1->10, 2->20
		if (page == null)
			page = 0;
		int startNum = page * 3;
		List<MainDto> boardsList = boardsDao.findAll(startNum);
		PagingDto paging = boardsDao.paging(page);

//		final int blockCount = 5;
//		int currentBlock = page / blockCount;
//		int startPageNum = 1 + blockCount * currentBlock;
//		int lastPageNum = 5 + blockCount * currentBlock;
//
//		if (paging.getTotalPage() < lastPageNum) {
//			lastPageNum = paging.getTotalPage();
//		}
//// 보드컨트롤러에서 paging.set~로 dto 완성

//		paging.setBlockCount(blockCount);
//		paging.setCurrentBlock(currentBlock);
//		paging.setStartPageNum(startPageNum);
//		paging.setLastPageNum(lastPageNum);
		paging.makeBlockInfo();
		
		model.addAttribute("paging", paging);
		model.addAttribute("boardsList", boardsList);
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
