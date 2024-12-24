/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.Producto.controller;

import com.example.Producto.model.Producto;
import com.example.Producto.servicio.ProductoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService productoService) {
        this.service = productoService;
    }

    @GetMapping
    public String listarProductos(Model model) {
        model.addAttribute("productos", this.service.listarTodos());
        return "productos";
    }

    @GetMapping("/nueva")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("producto", new Producto());
        return "formulario";
    }

    @PostMapping
    public String guardarProducto(@ModelAttribute Producto producto) {
        this.service.guardar(producto);
        return "redirect:/productos";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        model.addAttribute("producto", this.service.buscarPorId(id).orElseThrow(() -> new IllegalArgumentException("ID invalido" + id)));
        return "formulario";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id) {
        this.service.eliminar(id);  // Elimina el producto
        return "redirect:/productos";  // Redirige al listado de productos
    }

    @GetMapping("/reporte/pdf")
    public void generarPdf(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=productos_reporte.pdf");

        PdfWriter writer = new PdfWriter(response.getOutputStream());
        Document document = new Document(new com.itextpdf.kernel.pdf.PdfDocument(writer));

        document.add(new Paragraph("Reporte de productos").setBold().setFontSize(18));

        Table table = new Table(4);
        table.addCell("ID");
        table.addCell("Nombre");
        table.addCell("Precio");
        table.addCell("Cantidad");

        List<Producto> productos = this.service.listarTodos();
        for (Producto producto : productos) {
            table.addCell(producto.getId().toString());
            table.addCell(producto.getNombre());
            table.addCell(String.valueOf(producto.getPrecio()));
            table.addCell(String.valueOf(producto.getCantidad()));
        }

        document.add(table);
        document.close();
    }

    @GetMapping("/reporte/excel")
    public void generarExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=productos_reporte.xlsx");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Productos");
        Row headerRow = sheet.createRow(0);
        String[] columnHeaders = { "ID", "Nombre", "Precio", "Cantidad" };

        // Creando celdas de encabezado
        for (int i = 0; i < columnHeaders.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnHeaders[i]);
            CellStyle style = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font font = workbook.createFont();
            font.setBold(true);
            style.setFont(font);
            cell.setCellStyle(style);
        }

        // Llenando filas con los datos
        List<Producto> productos = this.service.listarTodos();
        int rowIndex = 1;
        for (Producto producto : productos) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(producto.getId());
            row.createCell(1).setCellValue(producto.getNombre());
            row.createCell(2).setCellValue(producto.getPrecio());
            row.createCell(3).setCellValue(producto.getCantidad());
        }

        // Autoajustando el tamaño de las columnas
        for (int i = 0; i < columnHeaders.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Escribiendo el archivo Excel en el flujo de salida
        workbook.write(response.getOutputStream());
        workbook.close();
    }
}
