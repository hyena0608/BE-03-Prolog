package com.prgrms.prolog.domain.comment.service;

import static com.prgrms.prolog.domain.comment.dto.CommentDto.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.prgrms.prolog.domain.comment.model.Comment;
import com.prgrms.prolog.domain.comment.repository.CommentRepository;
import com.prgrms.prolog.domain.post.model.Post;
import com.prgrms.prolog.domain.post.repository.PostRepository;
import com.prgrms.prolog.domain.user.model.User;
import com.prgrms.prolog.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CommentServiceImpl implements CommentService {

	private final CommentRepository commentRepository;
	private final PostRepository postRepository;
	private final UserRepository userRepository;

	@Override
	@Transactional
	public Long save(CreateCommentRequest request, String email, Long postId) {
		Post findPost = getFindPostBy(postId);
		User findUser = getFindUserBy(email);
		Comment comment = buildComment(request, findPost, findUser);
		return commentRepository.save(comment).getId();
	}

	@Override
	@Transactional
	public Long update(UpdateCommentRequest request, String email, Long commentId) {
		Comment findComment = commentRepository.joinUserByCommentId(commentId);
		validateCommentNotNull(findComment);
		validateCommentOwnerNotSameEmail(email, findComment);
		findComment.changeContent(request.content());
		return findComment.getId();
	}

	private Comment buildComment(CreateCommentRequest request, Post findPost, User findUser) {
		return Comment.builder()
			.content(request.content())
			.post(findPost)
			.user(findUser)
			.build();
	}

	private User getFindUserBy(String email) {
		return userRepository.findByEmail(email)
			.orElseThrow(() -> new IllegalArgumentException("exception.user.notExists"));
	}

	private Post getFindPostBy(Long postId) {
		return postRepository.findById(postId)
			.orElseThrow(() -> new IllegalArgumentException("exception.post.notExists"));
	}

	private void validateCommentOwnerNotSameEmail(String email, Comment  comment) {
		if (! comment.checkUserEmail(email)) {
			throw new IllegalArgumentException("exception.user.email.notSame");
		}
	}

	private void validateCommentNotNull(Comment comment) {
		Assert.notNull(comment, "exception.comment.notExists");
	}
}
