package com.nomina.controller;

import com.nomina.dao.EmpleadoDAO;
import com.nomina.excepciones.DatosNoCorrectosException;
import com.nomina.model.Empleado;

import java.io.IOException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Controlador principal para todas las peticiones web de empleados y nóminas
@WebServlet(name = "EmpleadoController", urlPatterns = {"/EmpleadoController"})
public class EmpleadoController extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    
    public EmpleadoController() {
        super();
    }

    // Gestiona las peticiones de tipo GET (mostrar listas, formularios y consultas)
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        String accion = request.getParameter("accion");

        // Si no se especifica ninguna acción, por defecto se muestra el listado
        if (accion == null || accion.isEmpty()) {
            accion = "listar";
        }

        EmpleadoDAO empleadoDAO = new EmpleadoDAO();

        try {
            // Opción para listar todos los empleados en la tabla
            if (accion.equals("listar")) {
                System.out.println("Usted ha seleccionado la opcion listar");
                List<Empleado> lista = empleadoDAO.obtenerTodos();
                request.setAttribute("empleados", lista);
                RequestDispatcher requestDispatcher =
                        request.getRequestDispatcher(
                                "/WEB-INF/views/listar.jsp");
                requestDispatcher.forward(request, response);

            // Opción para abrir el formulario de creación de un empleado nuevo
            } else if (accion.equals("nuevo") || accion.equals("crear")) {
                System.out.println("Usted ha seleccionado la opcion crear");
                RequestDispatcher requestDispatcher =
                        request.getRequestDispatcher(
                                "/WEB-INF/views/crear.jsp");
                requestDispatcher.forward(request, response);

            // Opción para cargar los datos de un empleado y mostrarlos en el formulario de edición
            } else if (accion.equals("editar")) {
                String dni = request.getParameter("dni");
                System.out.println("Editar empleado con DNI: " + dni);
                Empleado emp = empleadoDAO.obtenerPorDni(dni);
                request.setAttribute("empleado", emp);
                RequestDispatcher requestDispatcher =
                        request.getRequestDispatcher(
                                "/WEB-INF/views/editar.jsp");
                requestDispatcher.forward(request, response);

            // Opción para eliminar un empleado por su DNI y volver al listado
            } else if (accion.equals("eliminar")) {
                String dni = request.getParameter("dni");
                empleadoDAO.eliminar(dni);
                System.out.println("Registro eliminado satisfactoriamente...");
                response.sendRedirect("EmpleadoController?accion=listar");

            // Opción para abrir el formulario donde se introduce el dni para ver el salario
            } else if (accion.equals("buscarSalarioForm")) {
                System.out.println("Usted ha seleccionado buscar salario");
                RequestDispatcher requestDispatcher =
                        request.getRequestDispatcher("/WEB-INF/views/buscarSalario.jsp");
                requestDispatcher.forward(request, response);

            // Opción para calcular y mostrar el salario limpio de un empleado 
            } else if (accion.equals("mostrarSalario")) {
                String dni = request.getParameter("dni");
                System.out.println("Mostrando salario para el DNI: " + dni);
                Empleado emp = empleadoDAO.obtenerPorDni(dni);
                request.setAttribute("empleado", emp);
                RequestDispatcher requestDispatcher =
                        request.getRequestDispatcher("/WEB-INF/views/verSalario.jsp");
                requestDispatcher.forward(request, response);

            // Opción para abrir la vista de búsqueda avanzada
            } else if (accion.equals("busquedaAvanzada")) {
                System.out.println("Usted ha seleccionado búsqueda avanzada");
                RequestDispatcher requestDispatcher =
                            request.getRequestDispatcher("/WEB-INF/views/buscarModificar.jsp");
                requestDispatcher.forward(request, response);

            // Opción para filtrar y mostrar los resultados de la búsqueda avanzada
            } else if (accion.equals("resultadoBusqueda")) {
                String criterio = request.getParameter("criterio");
                System.out.println("Buscando empleados con criterio: " + criterio);
                
                List<Empleado> listaResultados = empleadoDAO.obtenerTodos();
                // Filtramos la lista según el criterio de dni o nombr 
                listaResultados.removeIf(emp -> !emp.getNombre().toLowerCase().contains(criterio.toLowerCase()) && 
                                                !emp.getDni().toLowerCase().contains(criterio.toLowerCase()));
                
                request.setAttribute("empleadosEncontrados", listaResultados);
                RequestDispatcher requestDispatcher =
                        request.getRequestDispatcher("/WEB-INF/views/resultadoBusqueda.jsp");
                requestDispatcher.forward(request, response);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Gestiona las peticiones de Post 
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        String accion = request.getParameter("accion");
        
        if (accion == null) {
            response.sendRedirect("EmpleadoController?accion=listar");
            return;
        }

        // Proceso para insertar un nuevo empleado en la base de datos
        if (accion.equals("insertar")) {
            EmpleadoDAO empleadoDAO = new EmpleadoDAO();
            try {
                String dni = request.getParameter("dni");
                String nombre = request.getParameter("nombre");
                char sexo = request.getParameter("sexo").charAt(0);
                int categoria = Integer.parseInt(
                        request.getParameter("categoria"));
                int anyos = Integer.parseInt(
                        request.getParameter("anyos_trabajados"));
                Empleado emp = new Empleado(
                        nombre, dni, sexo, categoria, anyos);
                empleadoDAO.insertar(emp);
                System.out.println("Registro guardado satisfactoriamente...");
                response.sendRedirect("EmpleadoController?accion=listar");
            } catch (DatosNoCorrectosException |
                    NumberFormatException e) {
                e.printStackTrace();
                request.setAttribute("error",
                        "Error en los datos introducidos: "
                        + e.getMessage());
                RequestDispatcher requestDispatcher =
                        request.getRequestDispatcher(
                                "/WEB-INF/views/crear.jsp");
                requestDispatcher.forward(request, response);
            }
        // Proceso para actualizar los datos de un empleado 
        } else if (accion.equals("actualizar")) {
            EmpleadoDAO empleadoDAO = new EmpleadoDAO();
            try {    
                String dni = request.getParameter("dni");
                String nombre = request.getParameter("nombre");
                char sexo = request.getParameter("sexo").charAt(0);
                int categoria = Integer.parseInt(
                        request.getParameter("categoria"));
                int anyos = Integer.parseInt(
                        request.getParameter("anyos_trabajados"));
                Empleado emp = new Empleado(
                        nombre, dni, sexo, categoria, anyos);
                empleadoDAO.actualizar(emp);
                System.out.println("Registro editado satisfactoriamente...");
                response.sendRedirect("EmpleadoController?accion=listar");

            } catch (DatosNoCorrectosException |
                    NumberFormatException e) {
                e.printStackTrace();
                request.setAttribute("error",
                        "Error al actualizar: " + e.getMessage());
                RequestDispatcher requestDispatcher =
                        request.getRequestDispatcher(
                                "/WEB-INF/views/editar.jsp");
                requestDispatcher.forward(request, response);
            }
        }
    }
}