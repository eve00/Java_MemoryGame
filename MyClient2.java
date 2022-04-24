import java.net.*;
import java.io.*;
import javax.swing.*;
import java.lang.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Timer;
import java.util.TimerTask;


public class MyClient2 extends JFrame implements MouseListener, MouseMotionListener {
    public int myTurn, myPoint, yourPoint;//

    public int myTime, yourTime, timeLimit, inputTime, min, sec;
    public ImageIcon[] randCard;
    public ImageIcon cardBack, joker;
    public JLabel mypointl, yourpointl, myTimel, yourTimel, turnl, idl, text_time, text_you, text_me, text_min, text_sec;
    public boolean isGameSet = false, isTimeOver = false, isStandby = false, isGameStart = false;
    PrintWriter out;//出力用のライター
    private int pairs = 0;
    private JPanel field, menuPanel;
    private JButton buttonArray[][];//ボタン用の配列
    private JButton shuffleButton, startButton;
    private String myId, yourId, strSec;
    private int[][] cardMemory = new int[2][2];
    private Container c;

    //フォント
    /*Font font;
    InputStream is = new FileInputStream("851letrogo_007.ttf");
        font = Font.createFont(Font.TRUETYPE_FONT, is);
        font = font.deriveFont(25f);//floatなので数字の後ろにfを足します。

        try {
            font = Font.createFont(Font.TRUETYPE_FONT, new File("851letrogo_007.ttf"));
            font = font.deriveFont(50f);//floatなので数字の後ろにfを足します。
        } catch (FontFormatException e) {
            System.out.println("形式がフォントではありません。");
        } catch (IOException e) {
            System.out.println("入出力エラーでフォントを読み込むことができませんでした。");
        }
    }*/


    public MyClient2() {
        //名前の入力ダイアログを開く
        String myName = JOptionPane.showInputDialog(null, "名前を入力してください", "名前の入力", JOptionPane.QUESTION_MESSAGE);
        String myIpAddress = JOptionPane.showInputDialog(null, "サーバのIPアドレスを入力してください", "サーバのIPアドレスの入力", JOptionPane.QUESTION_MESSAGE);
        if (myName.equals("")) {
            myName = "No name";//名前がないときは，"No name"とする
        }

        //ウィンドウを作成する
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//ウィンドウを閉じるときに，正しく閉じるように設定する
        setTitle("神経衰弱");//ウィンドウのタイトルを設定する
        setSize(1200, 690);//ウィンドウのサイズを設定する
        setBackground(Color.getColor("23793B"));
        ImageIcon bg = new ImageIcon("background.png");
        c = getContentPane();//フレームのペインを取得する

        //アイコンの設定

        //ゲーム画面
        //テーブル
        //カード置き場
        //獲得ポイント
        //持ち時間バー
        //カード54枚,裏面
        cardBack = new ImageIcon("Cards/back.png");
        joker = new ImageIcon("Cards/JOKER.png");
        randCard = new ImageIcon[54];

        for (int i = 0; i < 4; i++) {
            char mark = ' ';
            switch (i) {
                case 0:
                    mark = 'S';
                    break;
                case 1:
                    mark = 'C';
                    break;
                case 2:
                    mark = 'D';
                    break;
                case 3:
                    mark = 'H';
                    break;
            }
            for (int cardNum = 1, n = 0; cardNum <= 13; cardNum++, n++) {
                randCard[13 * i + n] = new ImageIcon("Cards/" + mark + cardNum + ".png");
            }
        }
        randCard[52] = joker;
        randCard[53] = joker;

        //cardMemory初期化
        Arrays.fill(cardMemory[0], -1);
        Arrays.fill(cardMemory[1], -1);

        //TODO 設定画面パネル
        //持ち時間設定
        //先攻・後攻
        //「ゲーム開始」ボタン
        //TODO 「ゲームの継続・変更」パネル
        //「このまま続ける」ボタン
        //「変更する」ボタン

        c.setLayout(null);//自動レイアウトの設定を行わない

        field = new JPanel();
        c.add(field);

        //ボタンの生成(カードの生成)
        buttonArray = new JButton[6][9];
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 9; j++) {
                buttonArray[i][j] = new JButton(cardBack);//ボタンにアイコンを設定する
                c.add(buttonArray[i][j]);//ペインに貼り付ける
                buttonArray[i][j].setBounds(254 + j * (75 + 2), 5 + i * (105 + 2), 75, 105);
                buttonArray[i][j].addMouseListener(this);//ボタンをマウスでさわったときに反応するようにする
                buttonArray[i][j].setActionCommand(Integer.toString(i * 9 + j));//ボタンに配列の情報を付加する（ネットワークを介してオブジェクトを識別するため）
            }
        }

        //シャッフルボタン
        shuffleButton = new JButton("shuffle");
        c.add(shuffleButton);
        shuffleButton.setBounds(1000, 0, 100, 100);
        shuffleButton.addMouseListener(this);

        //サーバに接続する
        Socket socket = null;
        try {
            //接続先の設定
            if (myIpAddress == null) {
                socket = new Socket("localhost", 10000);
            } else {
                socket = new Socket(myIpAddress, 10000);
            }
        } catch (UnknownHostException e) {
            System.err.println("ホストの IP アドレスが判定できません: " + e);
        } catch (IOException e) {
            System.err.println("エラーが発生しました: " + e);
        }

        MesgRecvThread mrt = new MesgRecvThread(socket, myName);//受信用のスレッドを作成する
        mrt.start();//スレッドを動かす（Runが動く）
    }

    public static void main(String[] args) {
        MyClient2 net = new MyClient2();
        net.setVisible(true);
    }

    public static void shuffle(ImageIcon[] array) {
        Random rnd = ThreadLocalRandom.current();
        for (int i = array.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // 要素入れ替え(swap)
            ImageIcon tmp = array[index];
            array[index] = array[i];
            array[i] = tmp;
        }
    }

    public void startGame(){
        //0.5秒遅延
        isStandby = true;//ゲームスタートの準備（オブジェクトの設置など）をする
        //３秒遅延
        //START!
        String msg = "GAMESTART";
    }

    public void flipJoker() {
        String msg;
        msg = "GIVEPOINT" + " " + myId;
        out.println(msg);
        out.flush();
        //カードを裏向けにする
        msg = "UNFLIP" + " " + cardMemory[0][1] + " " + cardMemory[1][1];
        out.println(msg);
        out.flush();
        //ターン終了
        msg = "END";
        out.println(msg);
        out.flush();

        System.out.println("初期化");
        Arrays.fill(cardMemory[0], -1);
        Arrays.fill(cardMemory[1], -1);
    }

    //TODO ゲーム終了時の処理

    static abstract class CountDown extends TimerTask {
        static int countDownTime;

        //インスタンス生成時入力された持ち時間を取得し代入
        public CountDown(int inputTime) {
            countDownTime = inputTime;
        }
    }

    class DrawPanel extends JLabel{
        public void paintComponent(Graphics g){
            super.paintComponent(g);

            g.setFont(new Font("Arial", Font.PLAIN, 25));
            g.drawString("持ち時間", 417, 224);
            g.drawString("分", 627, 224);
            g.drawString("秒", 742, 224);
            g.drawString("あなたが", 417, 296);
            g.drawString("相手が", 417, 351);
        }
    }

    public void mouseClicked(MouseEvent e) {//ボタンをクリックしたときの処理
        if(isGameStart) {
            if (myTurn == 0) {
                //相手のターンのときは何もしない
                System.out.println("相手のターン");
            } else {
                //自分のターンのとき
                JButton theButton = (JButton) e.getComponent();//クリックしたオブジェクトを得る．
                String msg = new String();

                //カードのオープン
                if (theButton == shuffleButton) {//シャッフルボタンが押されたとき
                    //カードをシャッフル
                    shuffle(randCard);
                    msg = new String();
                    for (ImageIcon image : randCard) {
                        msg = msg.substring(0, msg.length()) + image + " ";
                    }
                    msg = "SHUFFLE" + " " + msg;
                    out.println(msg);
                    out.flush();
                } else { //TODO (theIcon == cardBack)
                    String theArrayIndex = theButton.getActionCommand();//ボタンの配列の番号を取り出す
                    int theBnum = Integer.parseInt(theArrayIndex);//カードの位置
                    Icon theIcon = theButton.getIcon();//クリックしたボタンに設定されたアイコンを取得する
                    String theCard = randCard[theBnum].toString();

                    msg = "FLIP" + " " + theArrayIndex;
                    out.println(msg);
                    out.flush();

                    if (cardMemory[0][0] == -1) { //1枚目を開けたとき
                        //１枚目のカードの情報を記録
                        cardMemory[0][1] = theBnum;//カードの位置
                        if (theCard.equals("Cards/JOKER.png")) {
                            flipJoker();
                        } else {
                            cardMemory[0][0] = Integer.parseInt(theCard.replaceAll("[^0-9]", ""));//カードの数字
                        }
                        System.out.println("1枚目  " + "位置：" + cardMemory[0][1] + ", カード:" + ((theIcon == joker) ? joker : cardMemory[0][0]));
                    } else {//２枚目を開けたとき
                        if (cardMemory[0][1] != theBnum) {
                            //２枚目のカードの情報を記録
                            cardMemory[1][1] = theBnum;
                            if (theCard.equals("Cards/JOKER.png")) {
                                flipJoker();
                            } else {
                                cardMemory[1][0] = Integer.parseInt(theCard.replaceAll("[^0-9]", ""));//カードの数字
                            }
                            System.out.println("2枚目  " + "位置：" + cardMemory[1][1] + ", カード:" + ((theIcon == joker) ? joker : cardMemory[1][0]));

                            //一致・不一致の判定
                            if (cardMemory[0][0] == cardMemory[1][0]) { //一致
                                System.out.println("一致");
                                //2枚のカードの位置、ポイントを獲得するプレイヤー
                                //TODO myId → myTurn
                                //相手にポイントを与える
                                msg = "GETPOINT" + " " + cardMemory[0][1] + " " + cardMemory[1][1] + " " + myId;
                                out.println(msg);
                                out.flush();
                                //TODO カードのペアを非表示にする
                            } else { //不一致
                                System.out.println("不一致");
                                //カードを裏向けにする
                                msg = "UNFLIP" + " " + cardMemory[0][1] + " " + cardMemory[1][1];
                                out.println(msg);
                                out.flush();
                                //自分をターンを終わる
                                msg = "END";
                                out.println(msg);
                                out.flush();
                            }
                            //カードメモリを初期化
                            System.out.println("初期化");
                            Arrays.fill(cardMemory[0], -1);
                            Arrays.fill(cardMemory[1], -1);
                        }
                    }
                }

                repaint();//画面のオブジェクトを描画し直す
            }
        }
    }
    //メッセージ受信のためのスレッド
    public class MesgRecvThread extends Thread {


        Socket socket;
        String myName;

        public MesgRecvThread(Socket s, String n) {
            socket = s;
            myName = n;
        }

        //通信状況を監視し，受信データによって動作する
        public void run() {
            //接続時に実行される処理
            try {
                InputStreamReader sisr = new InputStreamReader(socket.getInputStream());
                BufferedReader br = new BufferedReader(sisr);
                out = new PrintWriter(socket.getOutputStream(), true);
                out.println(myName);//接続の最初に名前を送る
                boolean isGameSet = false;
                String myNumberStr = br.readLine();//サーバが送った番号を受け取る
                int myNumberInt = Integer.parseInt(myNumberStr);//文字を数字に変換する
                if (myNumberInt % 2 == 0) {//後攻（デフォルト）
                    myTurn = 0;
                    myId = "01";
                } else {//先攻（デフォルト）
                    myTurn = 1;
                    myId = "02";
                }
                myPoint = 0;
                yourPoint = 0;

                //ゲーム設定画面を表示(先に接続した方にゲームの設定をさせるとする)
                ImageIcon icon1 = new ImageIcon("Resources/menu.png");
                JLabel menuPanel = new JLabel(icon1);
                if(myId.equals("02")){
                    //ゲーム設定画面を表示
                    //DrawPanel menuPanel = new DrawPanel();
                    menuPanel.setBounds(350, 155, 500, 350);
                    JLabel menuWindow = new JLabel();
                    c.add(menuWindow);
                    menuWindow.setBounds(350, 155, 500, 350);
                    menuWindow.setOpaque(true);
                    menuWindow.setBackground(Color.white);

                    startButton = new JButton("START");

                    c.add(startButton);

                    //持ち時間の入力
                    //先攻後攻の入力
                    //ゲーム開始ボタン
                }else if(myId.equals("01")){
                    //相手が設定中です　と表示
                }

                if (isStandby) {//Startボタンを押したとき

                    //自分のポイント
                    mypointl = new JLabel("myPoint:" + myPoint);
                    c.add(mypointl);
                    mypointl.setFont(new Font("Arial", Font.PLAIN, 20));
                    mypointl.setForeground(Color.black);
                    mypointl.setBounds(1000, 100, 200, 100);

                    //相手のポイント
                    yourpointl = new JLabel("yourPoint:" + yourPoint);
                    c.add(yourpointl);
                    yourpointl.setFont(new Font("Arial", Font.PLAIN, 20));
                    yourpointl.setForeground(Color.black);
                    yourpointl.setBounds(1000, 200, 200, 100);

                    //時間
                    timeLimit = 300; //TODO　入力された値を取得する
                    min = timeLimit / 60;
                    sec = timeLimit % 60;
                    String strSec = String.format("%02d", sec);
                    //自分の時間
                    myTimel = new JLabel("my Time:" + min + ":" + strSec);
                    c.add(myTimel);
                    myTimel.setFont(new Font("Arial", Font.BOLD, 20));
                    myTimel.setForeground(Color.black);
                    myTimel.setBounds(1000, 300, 200, 100);
                    //相手の時間
                    yourTimel = new JLabel("your Time:" + min + ":" + strSec);
                    c.add(yourTimel);
                    yourTimel.setFont(new Font("Arial", Font.BOLD, 20));
                    yourTimel.setForeground(Color.black);
                    yourTimel.setBounds(100, 300, 200, 100);

                    //タイマー
                    Timer myTimer = new Timer(); //時間を計測するオブジェクトを生成


                    myTimer.scheduleAtFixedRate(new CountDown(inputTime) {//持ち時間のカウントダウンを行う
                        public void run() {
                            if (myTurn == 0) { //自分のターンのとき
                                //myTimeはCountDownのコンストラクタでinputTimeを受け取っている
                                CountDown.countDownTime--;
                                //サーバに時間をおくる
                                String msg = "TIME" + " " + CountDown.countDownTime + " " + myId;
                                out.println(msg);
                                out.flush();
                            }
                        }
                    }, 0, 1000);

                    //ターン
                    turnl = new JLabel(((myTurn == 1)
                            ? "あなたのターンです" + myTurn : "相手のターンです" + myTurn));
                    c.add(turnl);
                    turnl.setFont(new Font("Arial", Font.PLAIN, 20));
                    turnl.setForeground(Color.black);
                    turnl.setBounds(1000, 400, 200, 100);

                    //Id(test)
                    idl = new JLabel("ID:" + myId);
                    c.add(idl);
                    idl.setFont(new Font("Arial", Font.BOLD, 10));
                    idl.setForeground(Color.black);
                    idl.setBounds(1000, 500, 50, 50);
                }

                while (true) {
                    String inputLine = br.readLine();//データを一行分だけ読み込んでみる
                    if (inputLine != null) {//読み込んだときにデータが読み込まれたかどうかをチェックする
                        System.out.println(inputLine);//デバッグ（動作確認用）にコンソールに出力する
                        String[] inputTokens = inputLine.split(" ");    //入力データを解析するために、スペースで切り分ける
                        String cmd = inputTokens[0];//コマンドの取り出し．１つ目の要素を取り出す
                        String theId;
                        if(cmd.equals("GAMESTART")){
                            isGameStart = true;//ゲーム開始　操作可能になる
                        } else if (cmd.equals("TIME")) {
                            int time = Integer.parseInt(inputTokens[1]);
                            if (time < 0) {
                                String msg = "GAMESET";
                                out.println(msg);
                                out.flush();
                            }
                            theId = inputTokens[2];
                            min = time / 60;
                            sec = time % 60;
                            strSec = String.format("%02d", sec);
                            if (theId.equals(myId)) {
                                myTimel.setText("my Time:" + min + ":" + strSec);
                            } else {
                                yourTimel.setText("your Time:" + min + ":" + strSec);
                            }
                        } else if (cmd.equals("SHUFFLE")) {
                            //ImageIcon配列randCardを生成
                            for (int i = 1; i < inputTokens.length; i++) {
                                randCard[i - 1] = new ImageIcon(inputTokens[i]);
                            }
                            //カードにrandCardをsetIconする
                            for (int i = 0; i < 6; i++) {
                                for (int j = 0; j < 9; j++) {
                                    buttonArray[i][j].setIcon(randCard[i * 9 + j]);
                                }
                            }
                        } else if (cmd.equals("FLIP")) {
                            //引いたカードを表向きにする
                            int theBnum = Integer.parseInt(String.valueOf(inputTokens[1]));
                            int y = theBnum / 9;
                            int x = theBnum % 9;
                            System.out.println(x + "," + y + " randcard: " + randCard[theBnum]);
                            //カードの表面をsetIcon
                            buttonArray[y][x].setIcon(randCard[theBnum]);
                            System.out.println("FLIP/");
                        } else if (cmd.equals("UNFLIP")) {
                            //引いた2枚のカードを裏向ける
                            for (int i = 1; i <= 2; i++) {
                                int theBnum = Integer.parseInt(String.valueOf(inputTokens[i]));
                                if (theBnum == -1) break;
                                int y = theBnum / 9;
                                int x = theBnum % 9;
                                //カードの裏面をsetIcon
                                System.out.println(x + "," + y);
                                buttonArray[y][x].setIcon(cardBack);
                            }
                            System.out.print("UNFLIP/");
                        } else if (cmd.equals("GETPOINT")) {
                            //引いた2枚のカードを非表示
                            for (int i = 1; i <= 2; i++) {
                                int theBnum = Integer.parseInt(String.valueOf(inputTokens[i]));
                                int y = theBnum / 9;
                                int x = theBnum % 9;
                                buttonArray[y][x].setVisible(false);
                                pairs++;
                            }
                            //ポイント追加
                            theId = inputTokens[3];
                            if (theId.equals(myId)) {//自分が獲得した場合、自分のポイントを加算し表示し直す
                                myPoint++;
                                mypointl.setText("myPoint:" + myPoint);
                            } else {//相手が獲得した場合、相手のポイントを加算し表示し直す
                                yourPoint++;
                                yourpointl.setText("yourPoint:" + yourPoint);
                            }
                            System.out.println("myP/yourP" + myPoint + "/" + yourPoint);
                            System.out.println("GETPOINT/");
                        } else if (cmd.equals("GIVEPOINT")) {
                            theId = inputTokens[1];
                            if (theId.equals(myId)) {//相手のポイントに加算し表示し直す
                                yourPoint++;
                                yourpointl.setText("yourPoint:" + yourPoint);
                            } else {//自分のポイントに加算され、表示し直す
                                myPoint++;
                                mypointl.setText("myPoint:" + myPoint);
                            }
                            System.out.println("GIVEPOINT/");
                        } else if (cmd.equals("END")) {
                            if (pairs >= 26) {//26ペアがテーブル上からなくなったとき（残りJOKER2枚）
                                String msg = "GAMESET";
                                out.println(msg);
                                out.flush();
                            } else {
                                myTurn = 1 - myTurn;
                                turnl.setText((myTurn == 1)
                                        ? "あなたのターンです。" + myTurn : "相手のターンです" + myTurn);
                                System.out.println("END/");
                            }
                        } else if (cmd.equals("GAMESET")) {
                            isGameSet = true;
                        }

                        if (isGameSet) {//ゲーム終了時の処理
                            if (isTimeOver) {
                                //時間切れ時の演出
                            } else {
                                //カードカウント演出
                                //WIN・LOSE表示
                            }
                            //「もう一度遊ぶ」表示
                        }
                        repaint();//画面のオブジェクトを描画し直す
                    } else {
                        break;
                    }
                }
                socket.close();
            } catch (IOException e) {
                System.err.println("エラーが発生しました: " + e);
            }
        }
    }

    public void mouseEntered(MouseEvent e) {//マウスがオブジェクトに入ったときの処理
        /*System.out.println("マウスが入った")*/
    }

    public void mouseExited(MouseEvent e) {//マウスがオブジェクトから出たときの処理
        /*System.out.println("マウス脱出");*/
    }

    public void mousePressed(MouseEvent e) {//マウスでオブジェクトを押したときの処理（クリックとの違いに注意）
        /*System.out.println("マウスを押した");*/
    }

    public void mouseReleased(MouseEvent e) {//マウスで押していたオブジェクトを離したときの処理
        /*System.out.println("マウスを放した")*/
    }

    public void mouseDragged(MouseEvent e) {//マウスでオブジェクトとをドラッグしているときの処理

    }

    public void mouseMoved(MouseEvent e) {//マウスがオブジェクト上で移動したときの処理

    }
}