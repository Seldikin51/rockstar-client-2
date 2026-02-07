// common.glsl
// вспомогательные функции и константы для шейдеров в клиенте

/**
 * Вычисляет Signed Distance Function (SDF) для прямоугольника с возможностью
 * задания индивидуальных скруглений для каждого угла.
 *
 * @param CenterPosition - координата точки относительно центра прямоугольника.
 * @param Size - половина ширины и высоты прямоугольника (т.е. от центра до края).
 * @param Radius - радиусы скругления углов в следующем порядке:
 *                 (верхний левый, верхний правый, нижний правый, нижний левый).
 *
 * @return float - расстояние от точки до ближайшей поверхности прямоугольника.
 *                 Отрицательное значение — внутри, положительное — снаружи.
 */
float roundedBoxSDF(vec2 CenterPosition, vec2 Size, vec4 Radius) {
    // Ограничиваем радиусы, чтобы они не превышали половины сторон прямоугольника.
    vec2 halfSize = Size;
    Radius = min(Radius, vec4(halfSize.x, halfSize.y, halfSize.x, halfSize.y));

    // Выбираем радиус в зависимости от квадранта, в котором находится точка.
    Radius.xy = (CenterPosition.x > 0.0) ? Radius.xy : Radius.zw;
    Radius.x  = (CenterPosition.y > 0.0) ? Radius.x  : Radius.y;

    // Смещаем координаты на радиус и вычисляем расстояние.
    vec2 q = abs(CenterPosition) - Size + Radius.x;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - Radius.x;
}

/**
 * Я ебал меня сосали
 *
 * Вершины идут по часовой стрелке:
 * 0 — левый верхний (0, 0)
 * 1 — левый нижний (0, 1)
 * 2 — правый нижний (1, 1)
 * 3 — правый верхний (1, 0)
 */
const vec2[4] RECT_VERTICES_COORDS = vec2[] (
    vec2(0.0, 0.0),
    vec2(0.0, 1.0),
    vec2(1.0, 1.0),
    vec2(1.0, 0.0)
);

vec2 rvertexcoord(int id) {
    return RECT_VERTICES_COORDS[id % 4];
}

float rdist(vec2 pos, vec2 size, vec4 radius) {
    radius.xy = (pos.x > 0.0) ? radius.xy : radius.wz;
    radius.x  = (pos.y > 0.0) ? radius.x : radius.y;

    vec2 v = abs(pos) - size + radius.x;
    return min(max(v.x, v.y), 0.0) + length(max(v, 0.0)) - radius.x;
}

float ralpha(vec2 size, vec2 coord, vec4 radius, float smoothness) {
    vec2 center = size * 0.5;
    float dist = rdist(center - (coord * size), center - 1.0, radius);
    return 1.0 - smoothstep(1.0 - smoothness, 1.0, dist);
}
