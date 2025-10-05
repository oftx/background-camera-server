package github.oftx.backgroundcamera.service;

import github.oftx.backgroundcamera.dto.AuthRequestDto;
import github.oftx.backgroundcamera.dto.AuthResponseDto;
import github.oftx.backgroundcamera.dto.RegisterRequestDto;
import github.oftx.backgroundcamera.entity.SystemSetting; // 【新增】导入
import github.oftx.backgroundcamera.entity.User;
import github.oftx.backgroundcamera.repository.SystemSettingRepository; // 【新增】导入
import github.oftx.backgroundcamera.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional; // 【新增】导入

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final SystemSettingRepository systemSettingRepository; // 【新增】

    // 【修改】更新构造函数以注入新的Repository
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager, SystemSettingRepository systemSettingRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.systemSettingRepository = systemSettingRepository; // 【新增】
    }

    public AuthResponseDto register(RegisterRequestDto request) {
        // 【新增】在注册流程开始前，检查注册功能是否开启
        checkRegistrationAvailability();

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalStateException("Username already taken");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        String jwtToken = jwtService.generateToken(user);
        return new AuthResponseDto(jwtToken);
    }

    // 【新增】检查注册开关状态的私有方法
    private void checkRegistrationAvailability() {
        Optional<SystemSetting> settingOpt = systemSettingRepository.findById("ALLOW_REGISTRATION");

        // 如果设置存在，则解析其布尔值；如果不存在，则默认为false（关闭）
        boolean isRegistrationAllowed = settingOpt
                .map(setting -> Boolean.parseBoolean(setting.getSettingValue()))
                .orElse(false);

        if (!isRegistrationAllowed) {
            throw new IllegalStateException("Registration is currently disabled by the administrator.");
        }
    }

    public AuthResponseDto login(AuthRequestDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();
        String jwtToken = jwtService.generateToken(user);
        return new AuthResponseDto(jwtToken);
    }
}