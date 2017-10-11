package cn.m2c.scm.port.adapter.restful.web.classify;

import cn.m2c.common.MCode;
import cn.m2c.common.MResult;
import cn.m2c.scm.application.classify.GoodsClassifyApplication;
import cn.m2c.scm.application.classify.command.GoodsClassifyAddCommand;
import cn.m2c.scm.application.classify.command.GoodsClassifyModifyCommand;
import cn.m2c.scm.application.classify.query.GoodsClassifyQueryApplication;
import cn.m2c.scm.domain.NegativeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 商品分类
 */
@RestController
@RequestMapping("/goods/classify")
public class GoodsClassifyAgent {
    private final static Logger LOGGER = LoggerFactory.getLogger(GoodsClassifyAgent.class);

    @Autowired
    GoodsClassifyApplication goodsClassifyApplication;
    @Autowired
    GoodsClassifyQueryApplication goodsClassifyQueryApplication;

    /**
     * 增加商品分类
     *
     * @param parentClassifyName 一级分类名称(增加一级分类必传，二、三级分类不传)
     * @param subClassifyNames   子分类名称list的json字符传,["短袖","裙子"]
     * @param parentClassifyId   子分类上级分类的id(增加二、三级分类必传，一级分类不传)
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<MResult> addGoodsClassify(
            @RequestParam(value = "parentClassifyName", required = false) String parentClassifyName,
            @RequestParam(value = "subClassifyNames", required = false) String subClassifyNames,
            @RequestParam(value = "parentClassifyId", required = false) String parentClassifyId) {
        MResult result = new MResult(MCode.V_1);
        try {
            GoodsClassifyAddCommand command = new GoodsClassifyAddCommand(parentClassifyName, subClassifyNames, parentClassifyId);
            goodsClassifyApplication.addGoodsClassify(command);
            result.setStatus(MCode.V_200);
        } catch (NegativeException ne) {
            LOGGER.error("addGoodsClassify NegativeException e:", ne);
            result = new MResult(ne.getStatus(), ne.getMessage());
        } catch (Exception e) {
            LOGGER.error("addGoodsClassify Exception e:", e);
            result = new MResult(MCode.V_400, "添加商品分类失败");
        }
        return new ResponseEntity<MResult>(result, HttpStatus.OK);
    }

    /**
     * 修改商品分类
     *
     * @param classifyName
     * @return
     */
    @RequestMapping(value = "/{classifyId}", method = RequestMethod.PUT)
    public ResponseEntity<MResult> modifyGoodsClassify(
            @PathVariable("classifyId") String classifyId,
            @RequestParam(value = "classifyName", required = false) String classifyName) {
        MResult result = new MResult(MCode.V_1);
        try {
            GoodsClassifyModifyCommand command = new GoodsClassifyModifyCommand(classifyId, classifyName);
            goodsClassifyApplication.modifyGoodsClassifyName(command);
            result.setStatus(MCode.V_200);
        } catch (NegativeException ne) {
            LOGGER.error("modifyGoodsClassify NegativeException e:", ne);
            result = new MResult(ne.getStatus(), ne.getMessage());
        } catch (Exception e) {
            LOGGER.error("modifyGoodsClassify Exception e:", e);
            result = new MResult(MCode.V_400, "修改商品分类失败");
        }
        return new ResponseEntity<MResult>(result, HttpStatus.OK);
    }

    /**
     * 删除商品分类
     *
     * @param classifyId
     * @return
     */
    @RequestMapping(value = "/{classifyId}", method = RequestMethod.DELETE)
    public ResponseEntity<MResult> deleteGoodsClassify(
            @PathVariable("classifyId") String classifyId) {
        MResult result = new MResult(MCode.V_1);
        try {
            goodsClassifyApplication.deleteGoodsClassifyName(classifyId);
            result.setStatus(MCode.V_200);
        } catch (NegativeException ne) {
            LOGGER.error("deleteGoodsClassify NegativeException e:", ne);
            result = new MResult(ne.getStatus(), ne.getMessage());
        } catch (Exception e) {
            LOGGER.error("deleteGoodsClassify Exception e:", e);
            result = new MResult(MCode.V_400, "删除商品分类失败");
        }
        return new ResponseEntity<MResult>(result, HttpStatus.OK);
    }

    /**
     * 查询商品分类
     *
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<MResult> queryGoodsClassify() {
        MResult result = new MResult(MCode.V_1);
        try {
            List<Map> list = goodsClassifyQueryApplication.recursionQueryGoodsClassifyTree("-1");
            result.setContent(list);
            result.setStatus(MCode.V_200);
        } catch (Exception e) {
            LOGGER.error("queryGoodsClassify Exception e:", e);
            result = new MResult(MCode.V_400, "删除商品分类失败");
        }
        return new ResponseEntity<MResult>(result, HttpStatus.OK);
    }

    /**
     * 查询商品分类详情
     *
     * @return
     */
    @RequestMapping(value = "/{classifyId}", method = RequestMethod.GET)
    public ResponseEntity<MResult> queryGoodsClassifyDetail(@PathVariable("classifyId") String classifyId) {
        MResult result = new MResult(MCode.V_1);
        try {
            List<Map> list = goodsClassifyQueryApplication.recursionQueryGoodsClassifyTree("-1");
            result.setContent(list);
            result.setStatus(MCode.V_200);
        } catch (Exception e) {
            LOGGER.error("queryGoodsClassifyDetail Exception e:", e);
            result = new MResult(MCode.V_400, "删除商品分类失败");
        }
        return new ResponseEntity<MResult>(result, HttpStatus.OK);
    }
}
