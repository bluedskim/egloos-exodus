이글루스(혹은 그 외 블로그에서)에서 posting를 읽어와서 hugo에서 사용가능한 md포맷으로 다운로드 하고 hugo를 이용해서 html 생성.

Spring Integration 으로 posting Lister, html downloader, html-to-markdown transformer, hugo invoker를 분리한다.

<개발환경>
* IntelliJ
* Gradle5
* Lombok

<특징/제약사항>
* 삽입된 모든 이미지(img 태그)가 저장됨. 동일한 이미지가 2번 삽입되었다 하더라도 다른 이미지로 판단함 그래서 이름이 다른 이미지 파일 2개가 저장됨.
* 이미지들은 원본이 아닌 thumbnail임
* 그 외에 삽입된 파일들은 저장하지 않음(서버 용량 문제)
* 블로그 본문의 html 태그들을 그대로 사용함(그래서 폰트가 안예쁘게 보일 수 있음)
* 다운 받은 html을 이용해 Hugo 로 블로그를 생성함.
* 한번에 하나의 블로그만 다운로드 함(가령 aaa.egloos.com이 처리중이라면 bbb.egloos.com을 동시에 처리하지 않음)
* 2초 정도에 한개의 글을 다운로드

<해야 할 작업>
* ~~설정파일을 yaml로 변경~~
* ~~헤드 이미지 지원~~
* ~~category, tag지원~~
* ~~이미지 width 100% 제거~~
* ~~미리보기 지원 : ex) http://samba.iptime.org/~aaa~~
* ~~상세화면에서 내용가져와야 덧글, '이어지는 내용'(netyhobby.egloos.com) 처리 가능함.~~
* ~~웹인터페이스 제공~~
* ~~윈도우 컴퓨터에서 볼 수 있게~~
* ~~기본 헤드 이미지 자동 가져오기 : 꽤 힘듬. 어떤 CSS를 쓰는지 알기 힘들어 그래서 프로파일 이미지 가져오는 걸로 대체 함~~
* ~~중복된 블로그 제목의 경우 오류 발생~~
* ~~다운로드 완료시 압축파일 만들기~~
* ~~다운로드 완료시 이메일 발송(나에게도 발송)~~
* ~~제목에 역슬래시 있는 경우 hugo 생성시 오류 발생 http://nemonein.egloos.com/5317996~~
* ~~tag링크 오류~~
* ~~시스템 프로세스로 tgz압축하는 경우 파일이 너무 많으면 실패?~~
* ~~title에 " 없애기('로 변경)~~
* ~~파일명과 카테고리명에 # 없애기~~
* ~~블로그 base folder 명을 toplevel domain으로 통일함~~
* ~~블로그명을 입력받지 않고 html title 로 함~~
* ~~완료 이메일의 링크 변경~~
* ~~24시간이 지난 블로그 자동 삭제~~
* ~~내 블로그 합쳐서 생성해보기~~
* ~~대기열 제공(대기열을 저장하고 서버 재기동시 자동으로 resume~~
* 통계(현재까지 다운로드한 블로그 리스트, 총 블로그 개수, 총 용량 ...)

* 저장 공간 문제(하나의 블로그가 10G?)
* static 생성 오류 시 메일 보내고 작업 초기화
* theme : ugly url 기반일 때 category.html, tags.html 의 taxanomy에 링크 오류
* 하위 페이지에서 featured image경로 오류 http://samba.iptime.org/ee/A%20Citizen%20of%20(no)%20obscure%20city/public/posts/page/2.html
* posting별 featured_image적용 하기. 아마도 theme쪽에서 변경해야 할 듯
* 원본에 비해 폰트가 너무 큼
----------------------------
* 블로그 소유자만 다운로드 받을 수 있도록
* 모바일 사이트에서 다운로드 받는게 더 낫지 않나?
* 카테고리 목록
* live reload
* 미리보기
* 테마선택
* 제대로 로깅하기
** 불필요한 로깅 없애기 (html태그)
* 화면 리프레시 없이 정보변경가능하도록 web socket 사용
