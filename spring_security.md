# Tổng hợp Spring Security

## Sau một thời gian ôn spring security thì tôi hiểu như sau:

> *Spring security là trong 1 cái "SecurityFilterChain" được config, gồm nhiều filter, mỗi filter xử lý logic của riêng nó rồi gọi "chain.doFilter()" để xuống filter dưới xử lý tiếp.*

> *Trường hợp filter đang xử lý mà nhả exception thì bản thân filter đó có thể tự handle exception bằng try-catch hoặc ném trả lại exception đó lên cho filter trên xử lý hoặc dùng cơ chế exception handling trong spring security kết hợp những bean khác để xử lý, chẳng hạn như dùng "AuthenticationEntryPoint" hay "AccessDeniedHandler".*

---

## Với tư duy chung đó, tại Resource Server thì tôi hiểu luồng như này:

### (0) Request vào

### (1) Đi qua SecurityContextHolderFilter: khởi tạo SecurityContextHolder

> *Đây là Filter đứng đầu chuỗi*
>
> *Filter này hoạt động theo cơ chế "try-finally"*
>
> *Phần try sẽ bọc và chạy toàn bộ từ 2 → 11*

### (2) Đi qua CorsFilter

### (3) Đi qua CsrfFilter

### (4) Đi qua AuthenticationFilter, đến đây flow là: filter tạo Authentication → Manager điều phối → Provider xác thực → trả về Authentication đã xác thực → Set vào holder chung toàn request

> *"AuthenticationFilter" chỉ là gọi chung, thực nhất là dùng các class tương ứng theo config, chẳng hạn với config "http.oauth2resourceserver()" thì sẽ là class "BearerAuthenticationFilter", ngoài ra còn có "OAuth2LoginAuthenticationFilter", "AnonymousAuthenticationFilter", "UsernamePasswordAuthenticationFilter" ,...*
>
> *Sau khi gọi đích danh class, logic bên trong AuthenticationFilter sẽ được chạy, logic có thể sẽ khác nhau, nhưng đại khái nó đều tạo 1 authentication object chưa xác thực rồi gọi interface "AuthenticationManager" (được inject) bằng lệnh authenticationManager.authenticate() để delegate sang cho "ProviderManager" xử lý tiếp (gọi qua interface nhưng thực chất class implement nó sẽ được spring mò ra để chạy).*
>
> *Bản thân "ProviderManager" là 1 class implement interface "AuthenticationManager", nên nó có override method là authentication(), và đi kèm với đó là 1 field lưu lại một list các providers trong chính class, method authenticate() trong đây sẽ duyệt vòng for các provider này và tìm đúng provider cụ thể để ném việc sang cho nó làm.*
>
> *Đến đây provider thích hợp mới thực sự làm nhiệm vụ, các provider này sẽ implement interface chung đó là "AuthenticationProvider" gồm 2 method là "authenticate()" và "supports()", do đó toàn bộ provider sẽ có 2 override method này. Method supports() sẽ do ProviderManager dùng để tìm đúng provider thích hợp trước khi delegate sang cho provider đó (nếu tìm được) bằng lệnh provider.authenticate().*
>
> Đến đây, provider cụ thể mới chạy vào authentication() của nó để chạy logic xào nấu Authentication object thô, chưa xác thực do class AuthenticationFilter tạo như đã bàn ở trên, bên cạnh đó ta cũng có các provider tương ứng theo config chẳng hạn như:

| Provider                      | Dùng                                | Cho ra `authentication.getPrincipal()` (sau xử lý) |
|-------------------------------|-------------------------------------|----------------------------------------------------|
| **JwtAuthenticationProvider** | JWT decoder                         | `Jwt object`                                       |
| **DaoAuthenticationProvider** | UserDetailService + PasswordEncoder | `UserDetail object`                                |

> *Cuối cùng, provider sẽ kết thúc logic của mình bằng việc trả ngược lại "Authentication object" sau khi được xào nấu (đã xác thực), lúc này class AuthenticationFilter nhận lấy và set vào "SecurityContextHolder", phục vụ authorization.*

> *Spring dùng threadlocal, mỗi request được handle trên một thread riêng nên cái "SecurityContextHolder" sẽ tồn tại global suốt vòng đợi request cho tới khi bị clear, giúp authorize dễ dàng nhờ authentiation object vừa được nhét vào.*

### (5) Đi qua AnonymousFilter

### (6) Đi qua ExceptionTranslationFilter

> *Filter này chỉ có thể bắt các exception được ném ra từ các filter đứng sau nó*
>
> *Nếu request gặp lỗi ngay tại bước AuthenticationFilter (token gửi lên bị hết hạn hoặc sai format), thì cần xử lý thêm ở chính AuthenticationFilter*

### (7) Đi qua AuthorizationFilter: Request-level Authorization

> *AuthorizationFilter là một class, nó lấy ra Authentication object trong SecurityContextHolder thông qua một hàm Supplier<Authentication>, giúp lazy-load chỉ lôi authentication object ra từ SecurityContextHolder khi thực sự cần dùng đến.*

> *Sau đó nó gọi interface AuthorizationManager<T> (được inject, chứa method check trả về AuthorizationDecision object) bằng lệnh authorizationManager.check() để delegate sang cho RequestMatcherDelegatingAuthorizationManager xử lý tiếp (gọi qua interface nhưng thực chất class implement nó sẽ được spring mò ra để chạy).*

> *Bản thân RequestMatcherDelegatingAuthorizationManager là một class implement AuthorizationManager<T> interface, nó chứa 1 override method là check() và 1 field lưu lại một list các rule phân quyền được build từ file Security Config, method check() trong đây sẽ tiến hành duyệt vòng for các rules này và return ngay lập tức khi tìm thấy URL khớp đầu tiên, nếu khớp thì sẽ gọi "AuthorizationManager con" ra check, nếu duyệt hết rules mà không khớp thì mặc định từ chối, tất cả gói hết vào AuthorizationDecision object cho bên trên bóc ra check, thế nên thứ tự xếp các rules khi config là đặc biệt quan trọng, cụ thể ta sẽ áp dụng Nguyên tắc vàng: (Specific) ở trên, (General) ở dưới.*

> *Các AuthorizationManager con cũng là các class implement AuthorizationManager<T> interface, nhưng nó không cần lưu list rules nữa mà thay vào đó sẽ là một field riêng tùy theo rule là gì, và đương nhiên là sẽ có một override method là check(), logic bên trong sẽ lấy Authentication object ra xử lý, và logic có thể khác nhau tùy theo mục đích check là gì, cuối cùng trả về AuthorizationDecision object cho bên trên.*

> *Kết thúc chuỗi xử lý, AuthorizationFilter class sẽ nhận về AuthorizationDecision bbject, bóc dữ liệu trong đó thông qua .isGranted(), nếu là true thì doFilter() cho đi tiếp xuống (8), ngược lại là false thì sẽ nhả exception để (6) tóm lấy và xử lý.*

### (8) Đến DispatcherServlet

### (9) Controller

### (10) Đến Service: Method-level Authorization

> *Tận dụng SpringAOP - Proxy kết hợp việc sử dụng interface MethodInterceptor. Spring tạo ra các class tương ứng theo từng annotation để đánh chặn trước khi chạy method thật. Với @PreAuthorize thì là AuthorizationManagerBeforeMethodInterceptor, với @PostAuthorize thì là AuthorizationManagerAfterMethodInterceptor*

> *Các ...MethodInterceptor class này đều implement MethodInterceptor interface, nên chúng đều sở hữu override method invoke() với logic bên trong sẽ gọi interface AuthorizationManager<T> (được inject, chứa method check trả về AuthorizationDecision object) bằng lệnh authorizationManager.check() để delegate sang manager tương ứng xử lý thay.*

> *Các manager tương ứng này là một class cũng implement AuthorizationManager<T> interface, do đó nó cũng có override method check() nhả về AuthorizationDecision, và một field riêng là một Bộ xử lý ngôn ngữ SpEL (Spring Expression Language), và đi kèm với annotation cụ thể. Các manager này nên nhớ là: PreAuthorizeAuthorizationManager, PostAuthorizeAuthorizationManager, SecuredAuthorizationManager, Jsr250AuthorizationManager.*

> *Cuối cùng, AuthorizationManagerBeforeMethodInterceptor class sẽ nhận về AuthorizationDecision object, bóc dữ liệu trong đó thông qua .isGranted(), nếu là true thì (MethodInvocation) method.proceed() cho chạy method thật, còn false thì nhả AuthorizationDeniedException (AccessDeniedException). DispatcherServlet sẽ tóm lấy exception này để tự xử lý hoặc ném sang cho ControllerAdvice (GlobalExceptionHandler) xử lý thay.*

### (11) Đến Repository → query DB → Service → Controller → Response

### (12) Hết vòng đời, clear SecurityContextHolder

> *Tới đây là request kết thúc quay ngược trở ra "khối try" như đã nói ở bước (1)*
>
> *"Khối finally" của SecurityContextHolderFilter sẽ gọi SecurityContextHolder.clearContext(), tiếp tục lặp lại với các request sau*

---

## LƯU Ý

- **Authentication object trc xác thực** — lưu raw string: `principle = "username"`, `credentials = "password"/null nếu dùng jwt`, `isAuthenticated = false`, `authorities = []`, ...
- **Authentication object sau xác thực** — `credentials` chắc chắn null, `isAuthenticated = true`, `principle = UserDetail object / Jwt object`, ....

---

## Có 3 cách viết login sau khi tôi hiểu được những kiến thức ở trên:

**Cách 1:** *là hồi tôi mới học Spring Security, tôi chỉ đơn thuần là coi login như 1 API, gọi service, trong service findByEmail, check mật khẩu bằng passwordEncoder.matches(), rồi genToken() trả về cho người dùng hoặc nhả lỗi nếu đăng nhập không thành công*

**Cách 2:** *vẫn là coi login như 1 API, nhưng sẽ tận dụng kiến thức spring security hơn, dùng Authentication object, AuthenticationManager, UserDetailsService, set Authentication object đã xác thực vào SecurityContextHolder (cái set này hơi thừa thãi)*

**Cách 3:** *logic như cách 2 nhưng không phải 1 endpoint trong controller, mà toàn bộ logic sẽ được viết trong 1 Filter tôi tự custom rồi đặt nó trong SecurityFilterChain*
