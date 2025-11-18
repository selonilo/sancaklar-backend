package com.sc.sancaklar.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sc.sancaklar.configuration.jwt.JwtService;
import com.sc.sancaklar.exception.AlreadyExistException;
import com.sc.sancaklar.exception.AnErrorOccurredException;
import com.sc.sancaklar.exception.MailOrPasswordIncorrectException;
import com.sc.sancaklar.exception.NotFoundException;
import com.sc.sancaklar.model.data.PostHardyConstant;
import com.sc.sancaklar.model.dto.ResponseMessageModel;
import com.sc.sancaklar.model.dto.user.*;
import com.sc.sancaklar.model.entity.*;
import com.sc.sancaklar.model.mapper.UserMapper;
import com.sc.sancaklar.repository.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.security.SecureRandom;

@Service
public class AuthServiceImpl implements AuthService {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789.";
    private static final SecureRandom RANDOM = new SecureRandom();
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String from;
    @Autowired
    SpringTemplateEngine templateEngine;

    private final JPAQueryFactory queryFactory;

    private final QUserEntity query = QUserEntity.userEntity;

    public AuthServiceImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    public UserModel getById(Long id) {
        var optUser = userRepository.findById(id);
        if (optUser.isPresent()) {
            var userModel = UserMapper.mapTo(optUser.get());
            return userModel;
        } else {
            throw new NotFoundException(id.toString());
        }
    }

    public UserModel register(UserModel userModel) {
        var optUser = userRepository.findByEmail(userModel.getEmail());
        if (optUser.isPresent()) {
            throw new AlreadyExistException(userModel.getEmail());
        }
        UserEntity entity = UserMapper.mapTo(userModel);
        entity.setPassword(passwordEncoder.encode(entity.getPassword()));
        return UserMapper.mapTo(userRepository.save(entity));
    }

    public UserModel updateUser(UserModel userModel) {
        var optUser = userRepository.findById(userModel.getId());
        if (optUser.isPresent()) {
            var user = UserMapper.mapTo(userModel);
            user.setPassword(passwordEncoder.encode(userModel.getPassword()));
            return UserMapper.mapTo(userRepository.saveAndFlush(user));
        } else {
            throw new NotFoundException(userModel.getEmail());
        }
    }

    public TokenModel login(LoginModel loginModel) {
        UserEntity user = userRepository.findByEmail(loginModel.getMail())
                .orElseThrow(() -> new NotFoundException(loginModel.getMail()));

        if (!passwordEncoder.matches(loginModel.getPassword(), user.getPassword())) {
            throw new MailOrPasswordIncorrectException();
        }
        TokenModel tokenModel = new TokenModel();
        tokenModel.setToken(jwtService.generateToken(user));
        tokenModel.setUserId(user.getId());
        tokenModel.setUsername(user.getUsername());
        return tokenModel;
    }

    @Transactional
    public ResponseMessageModel refreshPassword(PasswordRefreshModel passwordRefreshModel) {
        var optUserEntity = userRepository.findByEmail(passwordRefreshModel.getMail());
        if (optUserEntity.isPresent()) {
            var userEntity = optUserEntity.get();
            var password = generateRandomPassword(8);
            userEntity.setPassword(passwordEncoder.encode(password));
            userRepository.saveAndFlush(userEntity);
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message);
                Context context = new Context();
                context.setVariable(PostHardyConstant.PASSWORD, password);
                context.setVariable(PostHardyConstant.USERNAME, userEntity.getUsername());
                String text = templateEngine.process(PostHardyConstant.MAIL_TEMPLATE, context);
                helper.setFrom(from);
                helper.setTo(userEntity.getEmail());
                helper.setSubject(PostHardyConstant.MAIL_SUBJECT);
                helper.setText(text, true);
                mailSender.send(message);
            } catch (MessagingException e) {
                throw new AnErrorOccurredException(e.getMessage());
            }
            ResponseMessageModel responseMessageModel = new ResponseMessageModel();
            responseMessageModel.setMessage(passwordRefreshModel.getMail() + PostHardyConstant.MAIL_SUCCESS);
            return responseMessageModel;
        } else {
            throw new NotFoundException(passwordRefreshModel.getMail());
        }
    }

    public static String generateRandomPassword(int length) {
        if (length < 1) {
            throw new IllegalArgumentException("Password length must be at least 1");
        }

        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = RANDOM.nextInt(CHARACTERS.length());
            password.append(CHARACTERS.charAt(randomIndex));
        }

        return password.toString();
    }

}
