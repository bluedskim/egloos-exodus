이글루스(혹은 그 외 블로그에서)에서 posting를 읽어와서 hugo에서 사용가능한 md포맷으로 다운로드 하고 hugo를 이용해서 html 생성.

Spring Integration 으로 posting Lister, html downloader, html-to-markdown transformer, hugo invoker를 분리한다.

<개발환경>
* IntelliJ
* Gradle5
* Lombok

<특징/제약사항>
* 삽입된 모든 이미지(img 태그)가 저장됨. 동일한 이미지가 2번 삽입되었다 하더라도 다른 이미지로 판단함 그래서 이름이 다른 이미지 파일 2개가 저장됨.
* 그 외에 삽입된 파일들은 저장하지 않음(서버 용량 문제)
* 블로그 본문의 html 태그들을 그대로 사용함(그래서 폰트가 안예쁘게 보일 수 있음)
* 다운 받은 html을 이용해 Hugo 로 블로그를 생성함.
* 한번에 하나의 블로그만 다운로드 함(가령 aaa.egloos.com이 처리중이라면 bbb.egloos.com을 동시에 처리할 수 없음)

<해야 할 작업>
* ~~설정파일을 yaml로 변경~~
* ~~헤드 이미지 지원~~
* ~~category, tag지원~~
* ~~이미지 width 100% 제거~~
* ~~미리보기 지원 : ex) http://samba.iptime.org/~aaa~~
* ~~상세화면에서 내용가져와야 덧글, '이어지는 내용'(netyhobby.egloos.com) 처리 가능함.~~
* 웹인터페이스 제공
* 기본 헤드 이미지 제공
* 다운로드 완료시 이메일 발송
* 24시간이 지난 블로그 자동 삭제
* 미리보기
* 테마선택