package com.verduleria.ui;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.Icon;

/**
 * Generador de íconos vectoriales procedurales de alta definición (HiDPI)
 * para la interfaz de Verdulería Jiménez.
 * No requiere archivos externos de imágenes y garantiza nitidez perfecta en cualquier escala.
 */
public final class Iconos {

    private Iconos() { }

    @FunctionalInterface
    public interface Dibujante {
        void dibujar(Graphics2D g2, int w, int h);
    }

    public static Icon crear(int ancho, int alto, Dibujante d) {
        return new Icon() {
            @Override public int getIconWidth() { return ancho; }
            @Override public int getIconHeight() { return alto; }
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.translate(x, y);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                d.dibujar(g2, ancho, alto);
                g2.dispose();
            }
        };
    }

    /** Logo representativo de Verdulería Jiménez: tazón verde con hojas y zanahoria. */
    public static Icon logo(int size) {
        return crear(size, size, (g2, w, h) -> {
            float s = w / 48f;
            // Tazón / canasta base en terracota
            g2.setColor(Tema.TERRACOTA);
            Arc2D canasta = new Arc2D.Float(6 * s, 22 * s, 36 * s, 22 * s, 180, 180, Arc2D.CHORD);
            g2.fill(canasta);

            // Hoja izquierda verde oscuro
            g2.setColor(Tema.VERDE_OSCURO);
            GeneralPath h1 = new GeneralPath();
            h1.moveTo(24 * s, 24 * s);
            h1.curveTo(12 * s, 18 * s, 8 * s, 8 * s, 18 * s, 6 * s);
            h1.curveTo(26 * s, 8 * s, 26 * s, 18 * s, 24 * s, 24 * s);
            g2.fill(h1);

            // Hoja centro/derecha verde salvia
            g2.setColor(Tema.VERDE_SALVIA);
            GeneralPath h2 = new GeneralPath();
            h2.moveTo(24 * s, 24 * s);
            h2.curveTo(20 * s, 14 * s, 26 * s, 4 * s, 34 * s, 8 * s);
            h2.curveTo(40 * s, 14 * s, 30 * s, 20 * s, 24 * s, 24 * s);
            g2.fill(h2);

            // Zanahoria estilizada
            g2.setColor(new Color(0xE0, 0x6D, 0x36));
            GeneralPath z = new GeneralPath();
            z.moveTo(21 * s, 14 * s);
            z.lineTo(29 * s, 17 * s);
            z.lineTo(27 * s, 30 * s);
            z.closePath();
            g2.fill(z);
        });
    }

    public static Icon inicio(int s, Color col) {
        return crear(s, s, (g2, w, h) -> {
            g2.setColor(col);
            g2.setStroke(new BasicStroke(w * 0.08f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            float f = w / 20f;
            GeneralPath p = new GeneralPath();
            p.moveTo(3 * f, 9 * f);
            p.lineTo(10 * f, 3 * f);
            p.lineTo(17 * f, 9 * f);
            p.lineTo(17 * f, 17 * f);
            p.lineTo(12 * f, 17 * f);
            p.lineTo(12 * f, 11 * f);
            p.lineTo(8 * f, 11 * f);
            p.lineTo(8 * f, 17 * f);
            p.lineTo(3 * f, 17 * f);
            p.closePath();
            g2.draw(p);
        });
    }

    public static Icon ventas(int s, Color col) {
        return crear(s, s, (g2, w, h) -> {
            g2.setColor(col);
            g2.setStroke(new BasicStroke(w * 0.08f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            float f = w / 20f;
            // Carrito
            GeneralPath p = new GeneralPath();
            p.moveTo(2 * f, 4 * f);
            p.lineTo(5 * f, 4 * f);
            p.lineTo(7.5f * f, 13 * f);
            p.lineTo(16 * f, 13 * f);
            p.lineTo(17.5f * f, 6 * f);
            p.lineTo(5.5f * f, 6 * f);
            g2.draw(p);
            // Ruedas
            g2.fill(new Ellipse2D.Float(7 * f, 15 * f, 2.5f * f, 2.5f * f));
            g2.fill(new Ellipse2D.Float(14 * f, 15 * f, 2.5f * f, 2.5f * f));
        });
    }

    public static Icon compras(int s, Color col) {
        return crear(s, s, (g2, w, h) -> {
            g2.setColor(col);
            g2.setStroke(new BasicStroke(w * 0.08f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            float f = w / 20f;
            // Camión / Abastos
            GeneralPath p = new GeneralPath();
            p.moveTo(2 * f, 6 * f);
            p.lineTo(12 * f, 6 * f);
            p.lineTo(12 * f, 14 * f);
            p.lineTo(2 * f, 14 * f);
            p.closePath();
            g2.draw(p);
            GeneralPath cabina = new GeneralPath();
            cabina.moveTo(12 * f, 9 * f);
            cabina.lineTo(16 * f, 9 * f);
            cabina.lineTo(18 * f, 12 * f);
            cabina.lineTo(18 * f, 14 * f);
            cabina.lineTo(12 * f, 14 * f);
            g2.draw(cabina);
            g2.fill(new Ellipse2D.Float(4.5f * f, 14 * f, 2.5f * f, 2.5f * f));
            g2.fill(new Ellipse2D.Float(14f * f, 14 * f, 2.5f * f, 2.5f * f));
        });
    }

    public static Icon productos(int s, Color col) {
        return crear(s, s, (g2, w, h) -> {
            g2.setColor(col);
            g2.setStroke(new BasicStroke(w * 0.08f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            float f = w / 20f;
            // Caja 3D
            GeneralPath p = new GeneralPath();
            p.moveTo(10 * f, 2.5f * f);
            p.lineTo(17.5f * f, 6.5f * f);
            p.lineTo(17.5f * f, 14.5f * f);
            p.lineTo(10 * f, 18.5f * f);
            p.lineTo(2.5f * f, 14.5f * f);
            p.lineTo(2.5f * f, 6.5f * f);
            p.closePath();
            g2.draw(p);
            // Aristas interiores
            g2.draw(new Line2D.Float(10 * f, 10.5f * f, 10 * f, 18.5f * f));
            g2.draw(new Line2D.Float(10 * f, 10.5f * f, 17.5f * f, 6.5f * f));
            g2.draw(new Line2D.Float(10 * f, 10.5f * f, 2.5f * f, 6.5f * f));
        });
    }

    public static Icon inventario(int s, Color col) {
        return crear(s, s, (g2, w, h) -> {
            g2.setColor(col);
            g2.setStroke(new BasicStroke(w * 0.08f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            float f = w / 20f;
            // Capas apiladas
            GeneralPath p1 = new GeneralPath();
            p1.moveTo(10 * f, 3 * f); p1.lineTo(17 * f, 6.5f * f); p1.lineTo(10 * f, 10 * f); p1.lineTo(3 * f, 6.5f * f); p1.closePath();
            g2.draw(p1);
            GeneralPath p2 = new GeneralPath();
            p2.moveTo(3 * f, 10.5f * f); p2.lineTo(10 * f, 14 * f); p2.lineTo(17 * f, 10.5f * f);
            g2.draw(p2);
            GeneralPath p3 = new GeneralPath();
            p3.moveTo(3 * f, 14.5f * f); p3.lineTo(10 * f, 18 * f); p3.lineTo(17 * f, 14.5f * f);
            g2.draw(p3);
        });
    }

    public static Icon clientes(int s, Color col) {
        return crear(s, s, (g2, w, h) -> {
            g2.setColor(col);
            g2.setStroke(new BasicStroke(w * 0.08f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            float f = w / 20f;
            // Usuario 1
            g2.draw(new Ellipse2D.Float(5 * f, 4 * f, 5 * f, 5 * f));
            GeneralPath u1 = new GeneralPath();
            u1.moveTo(2 * f, 16 * f);
            u1.curveTo(2 * f, 12 * f, 13 * f, 12 * f, 13 * f, 16 * f);
            g2.draw(u1);
            // Usuario 2 detrás
            g2.draw(new Ellipse2D.Float(12 * f, 3 * f, 4.5f * f, 4.5f * f));
            GeneralPath u2 = new GeneralPath();
            u2.moveTo(13 * f, 11 * f);
            u2.curveTo(15 * f, 11 * f, 18 * f, 13 * f, 18 * f, 15 * f);
            g2.draw(u2);
        });
    }

    public static Icon reportes(int s, Color col) {
        return crear(s, s, (g2, w, h) -> {
            g2.setColor(col);
            float f = w / 20f;
            // Barras de gráfico
            g2.fill(new RoundRectangle2D.Float(3 * f, 11 * f, 3 * f, 7 * f, 1 * f, 1 * f));
            g2.fill(new RoundRectangle2D.Float(8 * f, 7 * f, 3 * f, 11 * f, 1 * f, 1 * f));
            g2.fill(new RoundRectangle2D.Float(13 * f, 4 * f, 3 * f, 14 * f, 1 * f, 1 * f));
        });
    }

    public static Icon usuarios(int s, Color col) {
        return crear(s, s, (g2, w, h) -> {
            g2.setColor(col);
            g2.setStroke(new BasicStroke(w * 0.08f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            float f = w / 20f;
            g2.draw(new Ellipse2D.Float(6.5f * f, 3 * f, 7 * f, 7 * f));
            GeneralPath p = new GeneralPath();
            p.moveTo(3 * f, 17 * f);
            p.curveTo(3 * f, 12 * f, 17 * f, 12 * f, 17 * f, 17 * f);
            g2.draw(p);
        });
    }

    public static Icon cerrarSesion(int s, Color col) {
        return crear(s, s, (g2, w, h) -> {
            g2.setColor(col);
            g2.setStroke(new BasicStroke(w * 0.09f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            float f = w / 20f;
            // Puerta / marco
            GeneralPath p = new GeneralPath();
            p.moveTo(9 * f, 4 * f);
            p.lineTo(4 * f, 4 * f);
            p.lineTo(4 * f, 16 * f);
            p.lineTo(9 * f, 16 * f);
            g2.draw(p);
            // Flecha salida
            g2.draw(new Line2D.Float(8 * f, 10 * f, 17 * f, 10 * f));
            g2.draw(new Line2D.Float(13 * f, 6 * f, 17 * f, 10 * f));
            g2.draw(new Line2D.Float(13 * f, 14 * f, 17 * f, 10 * f));
        });
    }

    public static Icon buscar(int s, Color col) {
        return crear(s, s, (g2, w, h) -> {
            g2.setColor(col);
            g2.setStroke(new BasicStroke(w * 0.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            float f = w / 20f;
            g2.draw(new Ellipse2D.Float(3 * f, 3 * f, 9 * f, 9 * f));
            g2.draw(new Line2D.Float(10 * f, 10 * f, 16 * f, 16 * f));
        });
    }

    public static Icon mas(int s, Color col) {
        return crear(s, s, (g2, w, h) -> {
            g2.setColor(col);
            g2.setStroke(new BasicStroke(w * 0.13f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            float f = w / 20f;
            g2.draw(new Line2D.Float(10 * f, 4 * f, 10 * f, 16 * f));
            g2.draw(new Line2D.Float(4 * f, 10 * f, 16 * f, 10 * f));
        });
    }

    public static Icon editar(int s, Color col) {
        return crear(s, s, (g2, w, h) -> {
            g2.setColor(col);
            g2.setStroke(new BasicStroke(w * 0.09f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            float f = w / 20f;
            GeneralPath p = new GeneralPath();
            p.moveTo(13 * f, 3 * f); p.lineTo(17 * f, 7 * f); p.lineTo(7 * f, 17 * f); p.lineTo(3 * f, 17 * f); p.lineTo(3 * f, 13 * f); p.closePath();
            g2.draw(p);
            g2.draw(new Line2D.Float(11 * f, 5 * f, 15 * f, 9 * f));
        });
    }

    public static Icon alternar(int s, Color col) {
        return crear(s, s, (g2, w, h) -> {
            g2.setColor(col);
            g2.setStroke(new BasicStroke(w * 0.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            float f = w / 20f;
            g2.draw(new Arc2D.Float(4 * f, 4 * f, 12 * f, 12 * f, 130, -260, Arc2D.OPEN));
            g2.draw(new Line2D.Float(10 * f, 2 * f, 10 * f, 9 * f));
        });
    }

    public static Icon eliminar(int s, Color col) {
        return crear(s, s, (g2, w, h) -> {
            g2.setColor(col);
            g2.setStroke(new BasicStroke(w * 0.09f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            float f = w / 20f;
            g2.draw(new Line2D.Float(3 * f, 5 * f, 17 * f, 5 * f));
            g2.draw(new Line2D.Float(7 * f, 3 * f, 13 * f, 3 * f));
            GeneralPath p = new GeneralPath();
            p.moveTo(5 * f, 5 * f); p.lineTo(6 * f, 17 * f); p.lineTo(14 * f, 17 * f); p.lineTo(15 * f, 5 * f);
            g2.draw(p);
            g2.draw(new Line2D.Float(8.5f * f, 8 * f, 8.5f * f, 14 * f));
            g2.draw(new Line2D.Float(11.5f * f, 8 * f, 11.5f * f, 14 * f));
        });
    }

    public static Icon confirmar(int s, Color col) {
        return crear(s, s, (g2, w, h) -> {
            g2.setColor(col);
            g2.setStroke(new BasicStroke(w * 0.13f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            float f = w / 20f;
            GeneralPath p = new GeneralPath();
            p.moveTo(4 * f, 10 * f); p.lineTo(8 * f, 15 * f); p.lineTo(16 * f, 5 * f);
            g2.draw(p);
        });
    }

    public static Icon cancelar(int s, Color col) {
        return crear(s, s, (g2, w, h) -> {
            g2.setColor(col);
            g2.setStroke(new BasicStroke(w * 0.12f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            float f = w / 20f;
            g2.draw(new Line2D.Float(5 * f, 5 * f, 15 * f, 15 * f));
            g2.draw(new Line2D.Float(15 * f, 5 * f, 5 * f, 15 * f));
        });
    }

    public static Icon alerta(int s, Color col) {
        return crear(s, s, (g2, w, h) -> {
            g2.setColor(col);
            g2.setStroke(new BasicStroke(w * 0.09f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            float f = w / 20f;
            GeneralPath p = new GeneralPath();
            p.moveTo(10 * f, 2 * f); p.lineTo(18 * f, 17 * f); p.lineTo(2 * f, 17 * f); p.closePath();
            g2.draw(p);
            g2.draw(new Line2D.Float(10 * f, 7 * f, 10 * f, 12 * f));
            g2.fill(new Ellipse2D.Float(9 * f, 14 * f, 2 * f, 2 * f));
        });
    }

    public static Icon dinero(int s, Color col) {
        return crear(s, s, (g2, w, h) -> {
            g2.setColor(col);
            g2.setStroke(new BasicStroke(w * 0.08f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            float f = w / 20f;
            g2.draw(new Ellipse2D.Float(2 * f, 2 * f, 16 * f, 16 * f));
            g2.drawString("$", 7.5f * f, 14 * f);
        });
    }

    public static Icon exportar(int s, Color col) {
        return crear(s, s, (g2, w, h) -> {
            g2.setColor(col);
            g2.setStroke(new BasicStroke(w * 0.09f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            float f = w / 20f;
            g2.draw(new Line2D.Float(3 * f, 15 * f, 17 * f, 15 * f));
            g2.draw(new Line2D.Float(10 * f, 3 * f, 10 * f, 12 * f));
            g2.draw(new Line2D.Float(6 * f, 8 * f, 10 * f, 12 * f));
            g2.draw(new Line2D.Float(14 * f, 8 * f, 10 * f, 12 * f));
        });
    }

    public static Icon hoja(int s, Color col) {
        return crear(s, s, (g2, w, h) -> {
            g2.setColor(col);
            float f = w / 20f;
            GeneralPath p = new GeneralPath();
            p.moveTo(3 * f, 17 * f);
            p.curveTo(3 * f, 8 * f, 10 * f, 3 * f, 17 * f, 3 * f);
            p.curveTo(17 * f, 12 * f, 12 * f, 17 * f, 3 * f, 17 * f);
            g2.fill(p);
        });
    }

    /** Ícono de papelera para la acción eliminar. */
    public static Icon papelera(int s, Color col) {
        return crear(s, s, (g2, w, h) -> {
            g2.setColor(col);
            g2.setStroke(new BasicStroke(w * 0.09f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            float f = w / 20f;
            // Tapa
            g2.draw(new Line2D.Float(4 * f, 5 * f, 16 * f, 5 * f));
            g2.draw(new Line2D.Float(8 * f, 3 * f, 12 * f, 3 * f));
            // Cuerpo
            GeneralPath body = new GeneralPath();
            body.moveTo(5 * f, 5 * f);
            body.lineTo(6 * f, 17 * f);
            body.lineTo(14 * f, 17 * f);
            body.lineTo(15 * f, 5 * f);
            g2.draw(body);
            // Líneas interiores
            g2.draw(new Line2D.Float(10 * f, 7 * f, 10 * f, 15 * f));
            g2.draw(new Line2D.Float(8 * f,  7 * f,  7.5f * f, 15 * f));
            g2.draw(new Line2D.Float(12 * f, 7 * f, 12.5f * f, 15 * f));
        });
    }
}

