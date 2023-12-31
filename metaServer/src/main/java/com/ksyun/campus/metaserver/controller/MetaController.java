package com.ksyun.campus.metaserver.controller;

import com.ksyun.campus.metaserver.domain.StatInfo;
import com.ksyun.campus.metaserver.entity.DataTransferInfo;
import com.ksyun.campus.metaserver.services.MetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RestController("/")
public class MetaController {

    @Autowired
    private MetaService metaService;

    @RequestMapping("stats")
    public ResponseEntity stats(@RequestParam String path){
        StatInfo statInfo = metaService.getStats(path);
        if(statInfo == null) {
            return new ResponseEntity<>("无stats", HttpStatus.valueOf(500));

        }
        return new ResponseEntity(statInfo, HttpStatus.OK);
    }
    @RequestMapping("create")
    public ResponseEntity createFile(@RequestParam String path){
        if(metaService.create(path)) {
            return new ResponseEntity(HttpStatus.OK);
        } else {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @RequestMapping("mkdir")
    public ResponseEntity mkdir(@RequestParam String path){
        if(metaService.mkdir(path)) {
            return new ResponseEntity(HttpStatus.OK);
        } else {
            return new ResponseEntity<>("zookeeper连接失败或找不到对应结点", HttpStatus.valueOf(500));
        }
    }
    @RequestMapping("listdir")
    public ResponseEntity listdir(@RequestParam String path){
        List<StatInfo> list = metaService.listdir(path);
        if(list == null) {
            return new ResponseEntity<>("KeeperErrorCode = NoNode for " + path, HttpStatus.valueOf(500));

        }
        return ResponseEntity.ok(list);
    }
    @RequestMapping("delete")
    public ResponseEntity delete(@RequestParam String path){
        if(metaService.delete(path)) {
            return new ResponseEntity(HttpStatus.OK);
        } else {
            return new ResponseEntity<>("存在子节点无法删除", HttpStatus.valueOf(500));
        }
    }

    /**
     * 保存文件写入成功后的元数据信息，包括文件path、size、三副本信息等
     * @return
     */
    @RequestMapping("write")
    public ResponseEntity commitWrite(@RequestBody DataTransferInfo dataTransferInfo){
        if(metaService.write(dataTransferInfo)) {
            return new ResponseEntity(HttpStatus.OK);
        } else {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 根据文件path查询三副本的位置，返回客户端具体ds、文件分块信息
     * @param path
     * @return
     */
    @RequestMapping("open")
    public ResponseEntity open(@RequestParam String path){
        StatInfo statInfo = metaService.getStats(path);
        Random random = new Random();
        List<String> res = new ArrayList<>();
        statInfo.getReplicaData().forEach(e -> {
            res.add(e.dsNode);
        });

        String ip = res.get(random.nextInt(res.size()));
        if(statInfo == null) {
            return new ResponseEntity<>("无stats", HttpStatus.valueOf(500));

        }
        return new ResponseEntity(ip, HttpStatus.OK);
    }


    /**
     * 关闭退出进程
     */
    @RequestMapping("shutdown")
    public void shutdownServer(){
        System.exit(-1);
    }

}
