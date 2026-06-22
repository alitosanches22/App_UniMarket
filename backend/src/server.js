import express from "express";
import cors from "cors";
import dotenv from "dotenv";
import { consultar, pool } from "./db.js";

dotenv.config();

const app = express();
const puerto = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

function manejarError(res, error) {
  console.error(error);
  res.status(500).json({
    mensaje: "Ocurrio un error en el servidor.",
    detalle: error.message,
  });
}

function productoDesdeFila(fila) {
  return {
    id: fila.id,
    idVendedor: fila.seller_id,
    nombreVendedor: fila.seller_name,
    carreraVendedor: fila.seller_career,
    idCategoria: fila.category_id,
    categoria: fila.category_name,
    titulo: fila.title,
    descripcion: fila.description,
    precio: Number(fila.price),
    estado: fila.condition,
    estadoPublicacion: fila.status,
    cantidadImagenes: Number(fila.image_count || 0),
    tieneVideo: Boolean(fila.has_video),
    creadoEn: fila.created_at,
    actualizadoEn: fila.updated_at,
  };
}

app.get("/api/salud", async (_req, res) => {
  try {
    const resultado = await consultar("SELECT NOW() AS ahora");
    res.json({
      mensaje: "API UniMarket funcionando.",
      baseDatos: resultado.rows[0].ahora,
    });
  } catch (error) {
    manejarError(res, error);
  }
});

app.post("/api/auth/registro", async (req, res) => {
  const { nombre, apellido, correo, contrasena, carrera, telefono } = req.body;

  if (!nombre || !apellido || !correo || !contrasena || !carrera) {
    return res.status(400).json({ mensaje: "Faltan campos obligatorios." });
  }

  try {
    const resultado = await consultar(
      `
      INSERT INTO users (
        first_name,
        last_name,
        institutional_email,
        password_hash,
        career,
        phone
      )
      VALUES ($1, $2, $3, crypt($4, gen_salt('bf')), $5, $6)
      RETURNING id, first_name, last_name, institutional_email, career, phone
      `,
      [nombre, apellido, correo, contrasena, carrera, telefono || null],
    );

    const usuario = resultado.rows[0];
    res.status(201).json({
      id: usuario.id,
      nombre: usuario.first_name,
      apellido: usuario.last_name,
      correo: usuario.institutional_email,
      carrera: usuario.career,
      telefono: usuario.phone,
    });
  } catch (error) {
    if (error.code === "23505") {
      return res.status(409).json({ mensaje: "El correo ya esta registrado." });
    }
    manejarError(res, error);
  }
});

app.post("/api/auth/login", async (req, res) => {
  const { correo, contrasena } = req.body;

  if (!correo || !contrasena) {
    return res.status(400).json({ mensaje: "Ingresa correo y contrasena." });
  }

  try {
    const resultado = await consultar(
      `
      SELECT id, first_name, last_name, institutional_email, career, phone
      FROM users
      WHERE institutional_email = $1
      AND password_hash = crypt($2, password_hash)
      `,
      [correo, contrasena],
    );

    if (resultado.rowCount === 0) {
      return res.status(401).json({ mensaje: "Credenciales incorrectas." });
    }

    const usuario = resultado.rows[0];
    res.json({
      id: usuario.id,
      nombre: usuario.first_name,
      apellido: usuario.last_name,
      correo: usuario.institutional_email,
      carrera: usuario.career,
      telefono: usuario.phone,
    });
  } catch (error) {
    manejarError(res, error);
  }
});

app.get("/api/categorias", async (_req, res) => {
  try {
    const resultado = await consultar("SELECT id, name FROM categories ORDER BY id");
    res.json(
      resultado.rows.map((fila) => ({
        id: fila.id,
        nombre: fila.name,
      })),
    );
  } catch (error) {
    manejarError(res, error);
  }
});

app.put("/api/usuarios/:idUsuario", async (req, res) => {
  const { nombre, apellido, carrera, telefono } = req.body;

  if (!nombre || !apellido || !carrera) {
    return res.status(400).json({ mensaje: "Faltan datos del usuario." });
  }

  try {
    const resultado = await consultar(
      `
      UPDATE users
      SET first_name = $1,
          last_name = $2,
          career = $3,
          phone = $4
      WHERE id = $5
      RETURNING id, first_name, last_name, institutional_email, career, phone
      `,
      [nombre, apellido, carrera, telefono || null, req.params.idUsuario],
    );

    if (resultado.rowCount === 0) {
      return res.status(404).json({ mensaje: "Usuario no encontrado." });
    }

    const usuario = resultado.rows[0];
    res.json({
      id: usuario.id,
      nombre: usuario.first_name,
      apellido: usuario.last_name,
      correo: usuario.institutional_email,
      carrera: usuario.career,
      telefono: usuario.phone,
    });
  } catch (error) {
    manejarError(res, error);
  }
});

app.get("/api/productos", async (req, res) => {
  const { busqueda = "", categoriaId } = req.query;
  const parametros = [];
  const filtros = ["p.status = 'AVAILABLE'"];

  if (busqueda.trim()) {
    parametros.push(`%${busqueda.trim()}%`);
    filtros.push(`(p.title ILIKE $${parametros.length} OR p.description ILIKE $${parametros.length})`);
  }

  if (categoriaId) {
    parametros.push(categoriaId);
    filtros.push(`p.category_id = $${parametros.length}`);
  }

  try {
    const resultado = await consultar(
      `
      SELECT
        p.*,
        c.name AS category_name,
        (u.first_name || ' ' || u.last_name) AS seller_name,
        u.career AS seller_career,
        COUNT(pm.id) FILTER (WHERE pm.media_type = 'IMAGE') AS image_count,
        BOOL_OR(pm.media_type = 'VIDEO') AS has_video
      FROM products p
      JOIN categories c ON c.id = p.category_id
      JOIN users u ON u.id = p.seller_id
      LEFT JOIN product_media pm ON pm.product_id = p.id
      WHERE ${filtros.join(" AND ")}
      GROUP BY p.id, c.name, u.first_name, u.last_name, u.career
      ORDER BY p.created_at DESC
      `,
      parametros,
    );

    res.json(resultado.rows.map(productoDesdeFila));
  } catch (error) {
    manejarError(res, error);
  }
});

app.get("/api/usuarios/:idUsuario/productos", async (req, res) => {
  try {
    const resultado = await consultar(
      `
      SELECT
        p.*,
        c.name AS category_name,
        (u.first_name || ' ' || u.last_name) AS seller_name,
        u.career AS seller_career,
        COUNT(pm.id) FILTER (WHERE pm.media_type = 'IMAGE') AS image_count,
        BOOL_OR(pm.media_type = 'VIDEO') AS has_video
      FROM products p
      JOIN categories c ON c.id = p.category_id
      JOIN users u ON u.id = p.seller_id
      LEFT JOIN product_media pm ON pm.product_id = p.id
      WHERE p.seller_id = $1
      GROUP BY p.id, c.name, u.first_name, u.last_name, u.career
      ORDER BY p.created_at DESC
      `,
      [req.params.idUsuario],
    );

    res.json(resultado.rows.map(productoDesdeFila));
  } catch (error) {
    manejarError(res, error);
  }
});

app.get("/api/productos/:id", async (req, res) => {
  try {
    const resultado = await consultar(
      `
      SELECT
        p.*,
        c.name AS category_name,
        (u.first_name || ' ' || u.last_name) AS seller_name,
        u.career AS seller_career,
        COUNT(pm.id) FILTER (WHERE pm.media_type = 'IMAGE') AS image_count,
        BOOL_OR(pm.media_type = 'VIDEO') AS has_video
      FROM products p
      JOIN categories c ON c.id = p.category_id
      JOIN users u ON u.id = p.seller_id
      LEFT JOIN product_media pm ON pm.product_id = p.id
      WHERE p.id = $1
      GROUP BY p.id, c.name, u.first_name, u.last_name, u.career
      `,
      [req.params.id],
    );

    if (resultado.rowCount === 0) {
      return res.status(404).json({ mensaje: "Producto no encontrado." });
    }

    const media = await consultar(
      `
      SELECT id, media_type, media_url, sort_order
      FROM product_media
      WHERE product_id = $1
      ORDER BY sort_order, created_at
      `,
      [req.params.id],
    );

    res.json({
      ...productoDesdeFila(resultado.rows[0]),
      multimedia: media.rows.map((fila) => ({
        id: fila.id,
        tipo: fila.media_type,
        url: fila.media_url,
        orden: fila.sort_order,
      })),
    });
  } catch (error) {
    manejarError(res, error);
  }
});

app.post("/api/productos", async (req, res) => {
  const {
    idVendedor,
    idCategoria,
    titulo,
    descripcion,
    precio,
    estado,
    multimedia = [],
  } = req.body;

  if (!idVendedor || !idCategoria || !titulo || !descripcion || precio == null || !estado) {
    return res.status(400).json({ mensaje: "Faltan datos del producto." });
  }

  const cliente = await pool.connect();
  try {
    await cliente.query("BEGIN");

    const producto = await cliente.query(
      `
      INSERT INTO products (
        seller_id,
        category_id,
        title,
        description,
        price,
        condition
      )
      VALUES ($1, $2, $3, $4, $5, $6)
      RETURNING *
      `,
      [idVendedor, idCategoria, titulo, descripcion, precio, estado],
    );

    for (const [indice, item] of multimedia.entries()) {
      await cliente.query(
        `
        INSERT INTO product_media (product_id, media_type, media_url, sort_order)
        VALUES ($1, $2, $3, $4)
        `,
        [producto.rows[0].id, item.tipo, item.url, indice],
      );
    }

    await cliente.query("COMMIT");
    res.status(201).json({ id: producto.rows[0].id, mensaje: "Producto creado." });
  } catch (error) {
    await cliente.query("ROLLBACK");
    manejarError(res, error);
  } finally {
    cliente.release();
  }
});

app.put("/api/productos/:id", async (req, res) => {
  const { idCategoria, titulo, descripcion, precio, estado, multimedia = [] } = req.body;

  if (!idCategoria || !titulo || !descripcion || precio == null || !estado) {
    return res.status(400).json({ mensaje: "Faltan datos del producto." });
  }

  const cliente = await pool.connect();
  try {
    await cliente.query("BEGIN");

    const resultado = await cliente.query(
      `
      UPDATE products
      SET category_id = $1,
          title = $2,
          description = $3,
          price = $4,
          condition = $5
      WHERE id = $6
      RETURNING id
      `,
      [idCategoria, titulo, descripcion, precio, estado, req.params.id],
    );

    if (resultado.rowCount === 0) {
      await cliente.query("ROLLBACK");
      return res.status(404).json({ mensaje: "Producto no encontrado." });
    }

    await cliente.query("DELETE FROM product_media WHERE product_id = $1", [req.params.id]);

    for (const [indice, item] of multimedia.entries()) {
      await cliente.query(
        `
        INSERT INTO product_media (product_id, media_type, media_url, sort_order)
        VALUES ($1, $2, $3, $4)
        `,
        [req.params.id, item.tipo, item.url, indice],
      );
    }

    await cliente.query("COMMIT");
    res.json({ mensaje: "Producto actualizado." });
  } catch (error) {
    await cliente.query("ROLLBACK");
    manejarError(res, error);
  } finally {
    cliente.release();
  }
});

app.patch("/api/productos/:id/vendido", async (req, res) => {
  try {
    const resultado = await consultar(
      "UPDATE products SET status = 'SOLD' WHERE id = $1 RETURNING id",
      [req.params.id],
    );

    if (resultado.rowCount === 0) {
      return res.status(404).json({ mensaje: "Producto no encontrado." });
    }

    res.json({ mensaje: "Producto marcado como vendido." });
  } catch (error) {
    manejarError(res, error);
  }
});

app.delete("/api/productos/:id", async (req, res) => {
  try {
    const resultado = await consultar("DELETE FROM products WHERE id = $1 RETURNING id", [req.params.id]);

    if (resultado.rowCount === 0) {
      return res.status(404).json({ mensaje: "Producto no encontrado." });
    }

    res.json({ mensaje: "Producto eliminado." });
  } catch (error) {
    manejarError(res, error);
  }
});

app.get("/api/usuarios/:idUsuario/favoritos", async (req, res) => {
  try {
    const resultado = await consultar(
      `
      SELECT
        p.*,
        c.name AS category_name,
        (u.first_name || ' ' || u.last_name) AS seller_name,
        u.career AS seller_career,
        COUNT(pm.id) FILTER (WHERE pm.media_type = 'IMAGE') AS image_count,
        BOOL_OR(pm.media_type = 'VIDEO') AS has_video
      FROM favorites f
      JOIN products p ON p.id = f.product_id
      JOIN categories c ON c.id = p.category_id
      JOIN users u ON u.id = p.seller_id
      LEFT JOIN product_media pm ON pm.product_id = p.id
      WHERE f.user_id = $1
      GROUP BY p.id, c.name, u.first_name, u.last_name, u.career, f.created_at
      ORDER BY f.created_at DESC
      `,
      [req.params.idUsuario],
    );

    res.json(resultado.rows.map(productoDesdeFila));
  } catch (error) {
    manejarError(res, error);
  }
});

app.post("/api/usuarios/:idUsuario/favoritos/:idProducto", async (req, res) => {
  try {
    await consultar(
      `
      INSERT INTO favorites (user_id, product_id)
      VALUES ($1, $2)
      ON CONFLICT (user_id, product_id) DO NOTHING
      `,
      [req.params.idUsuario, req.params.idProducto],
    );

    res.status(201).json({ mensaje: "Producto agregado a favoritos." });
  } catch (error) {
    manejarError(res, error);
  }
});

app.delete("/api/usuarios/:idUsuario/favoritos/:idProducto", async (req, res) => {
  try {
    await consultar(
      "DELETE FROM favorites WHERE user_id = $1 AND product_id = $2",
      [req.params.idUsuario, req.params.idProducto],
    );

    res.json({ mensaje: "Producto eliminado de favoritos." });
  } catch (error) {
    manejarError(res, error);
  }
});

app.post("/api/conversaciones", async (req, res) => {
  const { idProducto, idComprador, idVendedor } = req.body;

  if (!idProducto || !idComprador || !idVendedor) {
    return res.status(400).json({ mensaje: "Faltan datos de la conversacion." });
  }

  try {
    const resultado = await consultar(
      `
      INSERT INTO conversations (product_id, buyer_id, seller_id)
      VALUES ($1, $2, $3)
      ON CONFLICT (product_id, buyer_id, seller_id)
      DO UPDATE SET updated_at = CURRENT_TIMESTAMP
      RETURNING id, product_id, buyer_id, seller_id
      `,
      [idProducto, idComprador, idVendedor],
    );

    res.status(201).json({
      id: resultado.rows[0].id,
      idProducto: resultado.rows[0].product_id,
      idComprador: resultado.rows[0].buyer_id,
      idVendedor: resultado.rows[0].seller_id,
    });
  } catch (error) {
    manejarError(res, error);
  }
});

app.get("/api/conversaciones/:idConversacion/mensajes", async (req, res) => {
  try {
    const resultado = await consultar(
      `
      SELECT
        m.id,
        m.sender_id,
        (u.first_name || ' ' || u.last_name) AS sender_name,
        m.body,
        m.created_at
      FROM messages m
      JOIN users u ON u.id = m.sender_id
      WHERE m.conversation_id = $1
      ORDER BY m.created_at
      `,
      [req.params.idConversacion],
    );

    res.json(
      resultado.rows.map((fila) => ({
        id: fila.id,
        idRemitente: fila.sender_id,
        remitente: fila.sender_name,
        contenido: fila.body,
        creadoEn: fila.created_at,
      })),
    );
  } catch (error) {
    manejarError(res, error);
  }
});

app.post("/api/conversaciones/:idConversacion/mensajes", async (req, res) => {
  const { idRemitente, contenido } = req.body;

  if (!idRemitente || !contenido) {
    return res.status(400).json({ mensaje: "Faltan datos del mensaje." });
  }

  try {
    const resultado = await consultar(
      `
      INSERT INTO messages (conversation_id, sender_id, body)
      VALUES ($1, $2, $3)
      RETURNING id, created_at
      `,
      [req.params.idConversacion, idRemitente, contenido],
    );

    await consultar(
      "UPDATE conversations SET updated_at = CURRENT_TIMESTAMP WHERE id = $1",
      [req.params.idConversacion],
    );

    res.status(201).json({
      id: resultado.rows[0].id,
      creadoEn: resultado.rows[0].created_at,
      mensaje: "Mensaje enviado.",
    });
  } catch (error) {
    manejarError(res, error);
  }
});

app.listen(puerto, () => {
  console.log(`API UniMarket escuchando en http://localhost:${puerto}`);
});
