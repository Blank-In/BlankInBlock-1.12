package com.gmail.ksw26141;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

//JavaPlugin은 필수, Listener는 onPlayerInteract(PlayerInteractEvent event)와 같은 이벤트 함수를 실행하기 위해 필요함
public class BlankInBlock extends JavaPlugin implements Listener{
	String PluginTitle=ChatColor.YELLOW+"["+ChatColor.WHITE+"BlankInBlock"+ChatColor.YELLOW+"] "+ChatColor.GRAY;//채팅창에서 보여줄 제목 ChatColor클래스를 이용해 마인크래프트 내에서 문자열에 색을 입힐 수 잇음
	BukkitScheduler scheduler = getServer().getScheduler();//원하는 시간 후에 작업을 실행하기 위한 스케줄러
	int second=5;//블럭 리젠 기본 설정 시간
	ArrayList<String> addList=new ArrayList<String>();//리젠시킬 블럭을 추가하고 있는 유저의 목록

	@Override
	public void onEnable() {//플러그인이 작동하기 시작할 때 (리로드가 끝나거나 서버 가동 시)
		if(!getConfig().isSet("blocktime")) {//블럭이 리젠되는 쿨타임 초기설정
			getConfig().set("blocktime", second);//콘픽 파일에 쿨타임 저장
		}
		second=getConfig().getInt("blocktime");//콘픽 파일에서 쿨타임 불러오기
		getServer().getPluginManager().registerEvents(this, this);//이벤트 함수를 실행시키기 위해 리스너 객체를 등록  (registerEvents(이벤트 함수가 있는 리스너 객체, 플러그인 객체))
		getLogger().info(PluginTitle+"작동 시작");//서버 커맨드창에 메세지 출력 (System.out.println()도 똑같이 동작함)
	}
	
	@Override
	public void onDisable() {//플러그인이 작동을 멈췄을 때 (리로드를 시작하거나 서버 종료 시)
		saveConfig();//콘픽 파일 저장 (저장에 실패할 시 이전에 마지막으로 저장된 이후 변경된 내용들은 사라짐)
		getLogger().info(PluginTitle+"작동 중지");
	}
	
	public void sendMessage(CommandSender sender,String Message) {//서버 커맨드 창과 유저에게 동시에 메세지를 띄우기 위한 함수
		sender.sendMessage(Message);//유저에게 메세지 출력
		getLogger().info(Message);//서버 커맨드 창에 메세지 출력
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] player){//명령어 함수 plugin.yml에 등록된 명령어를 플레이어가 입력 시 이 함수가 작동함
		//플레이어가 명령어로 /aaa 123 456 789 입력시 cmd 객체에는 해당 명령어에 관한 정보가 담겨오고 player객체에는 123, 456, 789가 담겨있다.
		if(blockCommand(sender, cmd, label, player)) {//제일 밑 함수
			return true;
		}
		return false;//모든 명령어 처리에서 false 반환시 최종적으로 false 반환. false값이 반환되면 plugin.yml 파일에서 플레이어가 입력한 명령어의 usage 부분이 출력이 됨.
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {//플레이어 마우스 액션 이벤트
	    Player user = event.getPlayer();
	    Action action = event.getAction();
	    if(action.equals(Action.LEFT_CLICK_BLOCK) && addList.contains(user.getName())){//만약 플레이어의 동작이 블럭을 바라보며 좌클릭 이며 플레이어가 리젠블럭을 추가시키고 있는 유저의 목록에 있다면
    		event.setCancelled(true);//해당 이벤트 캔슬 (블럭이 부서지는 등의 좌클릭으로 인해 생기는 반응들을 일어나지 않게 한다)
    		Block block=event.getClickedBlock();//좌클릭 된 블럭 객체
    		String code="block."+block.getX()+","+block.getY()+","+block.getZ();//콘픽에서 값을 불러오고 저장하기 위한 문자열. 블럭의 xyz가 1 1 1 일시 block.1,1,1 콘픽 파일의 block 아래의 1,1,1을 뜻한다.
    		if(getConfig().isSet(code)) {//만약 콘픽파일에 등록이 되어있다면
    			getConfig().set(code, null);//콘픽 파일에서 해당값을 지워버린다.
    			user.sendMessage(PluginTitle+block.getX()+", "+block.getY()+", "+block.getZ()+"의 블록리젠이 꺼졌습니다.");
    		}
    		else {
    			getConfig().set(code, true);//콘픽 파일에 해당값을 저장한다.
    			user.sendMessage(PluginTitle+block.getX()+", "+block.getY()+", "+block.getZ()+"의 블록리젠이 켜졌습니다.");
    		}
	    }
	}
	
	@EventHandler
	public void onBlockBreakEvent(BlockBreakEvent event) {//블럭 파괴 이벤트
		Block block = event.getBlock();
		String code="block."+block.getX()+","+block.getY()+","+block.getZ();//콘픽파일에서 값을 불러오기 위한 문자열
		if(getConfig().getBoolean(code)) {//콘픽 파일에 해당값이 true로 저장되어 있을시
		    Material type=block.getType();//해당 블럭의 블럭코드
		    scheduler.scheduleSyncDelayedTask(this, new Runnable() {//작업을 스케쥴에 등록한다.
	            @Override
	            public void run() {
	            	block.setType(type);//해당 블럭을 작업을 스케쥴에 넣기 전 저장한 블럭코드로 바꾼다. 만약 블럭 코드가 1이었을 시 파괴하는 순간 1을 기억해 놓은 후 블럭은 파괴되어 블럭코드가 0이 되고 등록한 작업이 해당 블럭(좌표)의 블럭코드를 기억해 놓은 1로 바꾼다.
	            }
	        }, second*20);//20==1초
		}
	}
	
	@EventHandler
	public void onBlockPlaceEvent(BlockPlaceEvent event) {//블럭 설치 이벤트 (블럭 리젠이 켜져있는 곳에 블럭의 설치를 막기 위한 함수)
		Block block = event.getBlock();
		String code="block."+block.getX()+","+block.getY()+","+block.getZ();//콘픽파일에서 값을 불러오기 위한 문자열
		if(getConfig().getBoolean(code)) {//만약 콘픽파일에 값이 true로 저장되어 있으면
			event.setCancelled(true);//해당 블럭 설치 이벤트를 취소한다 (설치는 없던 일이 되고 다시 인벤토리로 돌아감)
		}
	}

	public boolean blockCommand(CommandSender sender, Command cmd, String label, String[] player) { //명령어 함수
		if(cmd.getName().equalsIgnoreCase("blockchange")) {//명령어가 blockchange 이면
			if(player.length!=0) {//명령어 외에 쓸데없는 값을 넣을 시 false를 반환
				return false;
			}
			if(addList.contains(sender.getName())) {//리젠 블럭을 추가중인 목록에 플레이어가 있을경우
				addList.remove(sender.getName());//목록에서 제거
				sendMessage(sender, PluginTitle+"블럭 상태변경이 중지되었습니다.");
			}
			else {//리젠 블럭을 추가중인 목록에 플레이어가 없을경우
				addList.add(sender.getName());//목록에 추가
				sendMessage(sender, PluginTitle+"블럭 상태변경을 시작합니다.");
			}
			return true;
		}
		else if(cmd.getName().equalsIgnoreCase("blocktime")) {//명령어가 blocktime 이면
			if(player.length==0) {//들어온 값이 없다면
				sendMessage(sender, PluginTitle+"블럭 리젠 쿨타임 : "+second);//설정되어 있는 쿨타임 출력
			}
			else if(player.length==1) {//들어온 값이 하나라면
				int changetime=0;
				try {
					changetime=Integer.parseInt(player[0]);//int형으로 변환
				}
				catch(Exception e) {
					return false;//int형으로 변환중 오류
				}
				second=changetime;
				getConfig().set("blocktime", second);//콘픽 파일에 변경된 쿨타임 저장
				sendMessage(sender, PluginTitle+"블럭 리젠 쿨타임이 "+second+"초로 변경되었습니다.");
			}
			else {
				return false;
			}
			return true;
		}
		return false;
	}	
}