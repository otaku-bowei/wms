package com.wms.base.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.wms.base.domain.dto.SkuCreateDTO;
import com.wms.base.domain.dto.SkuUpdateDTO;
import com.wms.base.domain.query.SkuQuery;
import com.wms.base.domain.vo.SkuVO;
import com.wms.base.service.SkuService;
import com.wms.common.core.result.PageResult;
import com.wms.common.core.result.R;
import com.wms.common.log.annotation.OperationLog;
import com.wms.common.log.enums.OperationType;
import com.wms.common.security.annotation.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * SKU 管理接口
 *
 * @author WMS
 */
@Tag(name = "SKU 管理", description = "SKU 增删改查、批量导入导出")
@RestController
@RequestMapping("/api/v1/skus")
@RequiredArgsConstructor
public class SkuController {

    /** 导入模板表头：外层为列，内层为该列的表头层级 */
    private static final List<List<String>> TEMPLATE_HEAD = List.of(
            List.of("SKU编码"), List.of("SPU编码"), List.of("商品名称"), List.of("颜色"), List.of("尺码"),
            List.of("条形码"), List.of("重量(克)"), List.of("体积(毫升)"), List.of("长(mm)"),
            List.of("宽(mm)"), List.of("高(mm)"), List.of("保质期(天)"), List.of("类目"));

    private final SkuService skuService;

    @Operation(summary = "分页查询 SKU")
    @GetMapping
    @RequirePermission("sku:view")
    public R<PageResult<SkuVO>> page(SkuQuery query) {
        return R.ok(skuService.page(query));
    }

    @Operation(summary = "新增 SKU")
    @PostMapping
    @RequirePermission("sku:create")
    @OperationLog(module = "SKU", type = OperationType.CREATE, description = "新增 SKU")
    public R<Long> create(@Valid @RequestBody SkuCreateDTO dto) {
        return R.ok(skuService.createSku(dto));
    }

    @Operation(summary = "查询 SKU 详情")
    @GetMapping("/{id}")
    @RequirePermission("sku:view")
    public R<SkuVO> detail(@PathVariable("id") Long id) {
        return R.ok(skuService.getSkuDetail(id));
    }

    @Operation(summary = "修改 SKU")
    @PutMapping("/{id}")
    @RequirePermission("sku:edit")
    @OperationLog(module = "SKU", type = OperationType.UPDATE, description = "修改 SKU")
    public R<Void> update(@PathVariable("id") Long id, @Valid @RequestBody SkuUpdateDTO dto) {
        skuService.updateSku(id, dto);
        return R.<Void>ok();
    }

    @Operation(summary = "删除 SKU")
    @DeleteMapping("/{id}")
    @RequirePermission("sku:delete")
    @OperationLog(module = "SKU", type = OperationType.DELETE, description = "删除 SKU")
    public R<Void> delete(@PathVariable("id") Long id) {
        skuService.deleteSku(id);
        return R.<Void>ok();
    }

    @Operation(summary = "启用/停用 SKU")
    @PutMapping("/{id}/status")
    @RequirePermission("sku:edit")
    @OperationLog(module = "SKU", type = OperationType.UPDATE, description = "启用停用 SKU")
    public R<Void> changeStatus(@PathVariable("id") Long id, @RequestParam("status") Integer status) {
        skuService.changeStatus(id, status);
        return R.<Void>ok();
    }

    @Operation(summary = "下载导入模板")
    @GetMapping("/template")
    @RequirePermission("sku:import")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        writeExcel(response, "SKU导入模板", TEMPLATE_HEAD, List.<List<Object>>of());
    }

    @Operation(summary = "批量导入 SKU")
    @PostMapping("/import")
    @RequirePermission("sku:import")
    @OperationLog(module = "SKU", type = OperationType.IMPORT, description = "批量导入 SKU")
    public R<Map<String, Object>> importSkus(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return R.ok(Map.of("totalCount", 0, "successCount", 0, "failCount", 0));
        }
        return R.ok(skuService.importSkus(file.getInputStream()));
    }

    @Operation(summary = "批量导出 SKU")
    @PostMapping("/export")
    @RequirePermission("sku:export")
    @OperationLog(module = "SKU", type = OperationType.EXPORT, description = "批量导出 SKU")
    public void export(HttpServletResponse response, SkuQuery query) throws IOException {
        List<SkuVO> list = skuService.listForExport(query);
        List<List<String>> head = TEMPLATE_HEAD;
        List<List<Object>> data = list.stream()
                .map(vo -> List.<Object>of(
                        vo.getSkuCode(), vo.getSpuCode(), vo.getSkuName(), vo.getColor(), vo.getSize(),
                        vo.getBarcodes() == null ? "" : String.join(",", vo.getBarcodes()),
                        vo.getWeight(), vo.getVolume(), vo.getLengthMm(), vo.getWidthMm(),
                        vo.getHeightMm(), vo.getShelfLifeDays(), vo.getCategoryName()))
                .toList();
        writeExcelData(response, "SKU导出", head, data);
    }

    private void writeExcel(HttpServletResponse response, String fileName,
                            List<List<String>> head, List<List<Object>> data) throws IOException {
        writeExcelData(response, fileName, head, data);
    }

    private void writeExcelData(HttpServletResponse response, String fileName,
                                List<List<String>> head, List<List<Object>> data) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition",
                "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + ".xlsx");
        EasyExcel.write(response.getOutputStream())
                .head(head)
                .sheet("Sheet1")
                .doWrite(data);
    }

    @SuppressWarnings("unused")
    private WriteCellStyle defaultStyle() {
        return new WriteCellStyle();
    }
}
