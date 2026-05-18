let moreblock_export_action;
let moreblock_generate_hitbox_action;

(function() {
    const PLUGIN_ID = 'moreblock_blockbench_tools';
    const TEXT = {
        zh: {
            dialogTitle: '导出 MoreBlock 配置 JSON',
            blockId: '方块 ID',
            chineseName: '中文名称',
            englishName: '英文名称',
            geoFile: '模型文件',
            textureFile: '贴图文件',
            displayFile: '展示文件',
            lightLevel: '亮度',
            canSit: '可坐下',
            seatHeight: '坐下高度',
            exportType: 'MoreBlock 配置 JSON',
            exported: 'MoreBlock 配置已导出',
            pluginTitle: 'MoreBlock Blockbench 工具',
            pluginDescription: '从 Blockbench 导出 MoreBlock 方块配置 JSON，并自动生成 MoreBlock 可识别的 hitbox。',
            actionName: '导出 MoreBlock 配置 JSON',
            actionDescription: '创建 MoreBlock 方块配置 JSON 文件',
            hitboxActionName: '生成 MoreBlock Hitbox',
            simpleHitboxActionName: '生成简单 Hitbox',
            complexHitboxActionName: '生成复杂 Hitbox',
            qualityHitboxActionName: '生成高质量 Hitbox',
            greedyHitboxActionName: '生成 Greedy Hitbox',
            hitboxActionDescription: '根据当前模型自动创建 MoreBlock 可识别的 hitbox 骨骼',
            hitboxDialogTitle: '生成 MoreBlock Hitbox',
            hitboxMode: '生成模式',
            simpleMode: '简单模式：整体大盒子',
            complexMode: '复杂模式：快速体素合并',
            qualityMode: '高质量模式：全局最大盒覆盖',
            greedyMode: 'Greedy 模式：切片矩形合并',
            simplification: '简化程度',
            simplificationInfo: '数值越小越精细，生成盒子可能更多；数值越大越简单。建议 1。高质量和 Greedy 模式会更慢。',
            maxBoxes: '最大盒子数量',
            maxBoxesInfo: '超过数量后会保留体积最大的盒子。高质量模式会按最大体积逐个提取，Greedy 模式会按三轴切片候选选择。',
            hitboxGenerated: 'MoreBlock hitbox 已生成',
            hitboxGeneratedWithCount: count => `MoreBlock hitbox 已生成：${count} 个盒子`,
            noModelCubes: '当前模型没有可用于计算 hitbox 的方块',
            defaultChineseName: '自定义方块',
            defaultEnglishName: 'Custom Block'
        },
        en: {
            dialogTitle: 'Export MoreBlock Config JSON',
            blockId: 'Block ID',
            chineseName: 'Chinese Name',
            englishName: 'English Name',
            geoFile: 'Geo File',
            textureFile: 'Texture File',
            displayFile: 'Display File',
            lightLevel: 'Light Level',
            canSit: 'Can Sit',
            seatHeight: 'Seat Height',
            exportType: 'MoreBlock Config JSON',
            exported: 'MoreBlock config exported',
            pluginTitle: 'MoreBlock Blockbench Tools',
            pluginDescription: 'Export a MoreBlock block config JSON from Blockbench and generate a MoreBlock hitbox bone.',
            actionName: 'Export MoreBlock Config JSON',
            actionDescription: 'Create a MoreBlock block config JSON file',
            hitboxActionName: 'Generate MoreBlock Hitbox',
            simpleHitboxActionName: 'Generate Simple Hitbox',
            complexHitboxActionName: 'Generate Complex Hitbox',
            qualityHitboxActionName: 'Generate High Quality Hitbox',
            greedyHitboxActionName: 'Generate GitHub Greedy Hitbox',
            hitboxActionDescription: 'Create a MoreBlock-compatible hitbox bone from the current model',
            hitboxDialogTitle: 'Generate MoreBlock Hitbox',
            hitboxMode: 'Generation Mode',
            simpleMode: 'Simple: One Bounding Box',
            complexMode: 'Complex: Fast Voxel Merge',
            qualityMode: 'High Quality: Global Largest-Box Cover',
            greedyMode: 'GitHub Greedy: Sliced Rectangle Merge',
            simplification: 'Simplification',
            simplificationInfo: 'Lower values are more accurate and may create more boxes. Higher values are simpler. 1 is recommended. High quality and GitHub Greedy modes are slower.',
            maxBoxes: 'Max Boxes',
            maxBoxesInfo: 'If the result exceeds this number, only the largest boxes are kept. High quality extracts the largest box each round, and GitHub Greedy chooses candidates from three-axis slices.',
            hitboxGenerated: 'MoreBlock hitbox generated',
            hitboxGeneratedWithCount: count => `MoreBlock hitbox generated: ${count} boxes`,
            noModelCubes: 'No model cubes are available for hitbox calculation',
            defaultChineseName: '自定义方块',
            defaultEnglishName: 'Custom Block'
        }
    };

    function getLanguageCode() {
        const candidates = [];
        if (typeof Settings !== 'undefined' && Settings && Settings.get) {
            candidates.push(Settings.get('language'));
        }
        if (typeof StateMemory !== 'undefined' && StateMemory) {
            candidates.push(StateMemory.language);
        }
        if (typeof Blockbench !== 'undefined' && Blockbench) {
            candidates.push(Blockbench.language);
        }
        if (typeof navigator !== 'undefined' && navigator) {
            candidates.push(navigator.language);
        }
        const value = candidates.find(candidate => typeof candidate === 'string' && candidate.trim());
        return String(value || 'en').toLowerCase();
    }

    function getText() {
        return getLanguageCode().startsWith('zh') ? TEXT.zh : TEXT.en;
    }

    function sanitizeId(value) {
        return String(value || '')
            .trim()
            .toLowerCase()
            .replace(/[^a-z0-9_\-]+/g, '_')
            .replace(/_+/g, '_')
            .replace(/^_+|_+$/g, '') || 'custom_block';
    }

    function getProjectBaseName() {
        if (typeof Project !== 'undefined' && Project && Project.name) {
            return String(Project.name).replace(/\.geo\.json$/i, '').replace(/\.json$/i, '');
        }
        if (typeof Format !== 'undefined' && Format && Format.id) {
            return Format.id;
        }
        return 'custom_block';
    }

    function getDefaultGeoFile() {
        const baseName = getProjectBaseName();
        return baseName.toLowerCase().endsWith('.geo') ? `${baseName}.json` : `${baseName}.geo.json`;
    }

    function buildConfig(form) {
        const text = getText();
        const config = {
            id: sanitizeId(form.id),
            name: {
                zh_cn: String(form.zh_cn || '').trim() || text.defaultChineseName,
                en_us: String(form.en_us || '').trim() || text.defaultEnglishName
            },
            geo: String(form.geo || '').trim() || getDefaultGeoFile(),
            texture: String(form.texture || '').trim() || 'texture.png',
            light_level: Math.max(0, Math.min(15, Number.parseInt(form.light_level, 10) || 0)),
            supports_sitting: Boolean(form.supports_sitting),
            seat_height: Number.isFinite(Number.parseFloat(form.seat_height)) ? Number.parseFloat(form.seat_height) : 0.5
        };

        const display = String(form.display || '').trim();
        if (display) {
            config.display = display;
        }
        return config;
    }

    function readVector(value, fallback) {
        if (Array.isArray(value) && value.length >= 3) {
            return [Number(value[0]) || 0, Number(value[1]) || 0, Number(value[2]) || 0];
        }
        return fallback.slice();
    }

    function getCubeBounds(cube) {
        const from = readVector(cube.from, [0, 0, 0]);
        const to = readVector(cube.to, [0, 0, 0]);
        const inflate = Number(cube.inflate) || 0;
        return {
            minX: Math.min(from[0], to[0]) - inflate,
            minY: Math.min(from[1], to[1]) - inflate,
            minZ: Math.min(from[2], to[2]) - inflate,
            maxX: Math.max(from[0], to[0]) + inflate,
            maxY: Math.max(from[1], to[1]) + inflate,
            maxZ: Math.max(from[2], to[2]) + inflate
        };
    }

    function getSourceCubes() {
        if (typeof Cube === 'undefined' || !Cube.all || !Cube.all.length) {
            return [];
        }
        return Cube.all.filter(cube => !(cube.parent && cube.parent.name === 'hitbox'));
    }

    function calculateBoundsFromBoxes(boxes) {
        if (!boxes.length) {
            return null;
        }
        return boxes.reduce((bounds, box) => {
            if (!bounds) {
                return Object.assign({}, box);
            }
            return {
                minX: Math.min(bounds.minX, box.minX),
                minY: Math.min(bounds.minY, box.minY),
                minZ: Math.min(bounds.minZ, box.minZ),
                maxX: Math.max(bounds.maxX, box.maxX),
                maxY: Math.max(bounds.maxY, box.maxY),
                maxZ: Math.max(bounds.maxZ, box.maxZ)
            };
        }, null);
    }

    function getBoxVolume(box) {
        return Math.max(0, box.maxX - box.minX) * Math.max(0, box.maxY - box.minY) * Math.max(0, box.maxZ - box.minZ);
    }

    function intersects(a, b) {
        return a.minX < b.maxX && a.maxX > b.minX
            && a.minY < b.maxY && a.maxY > b.minY
            && a.minZ < b.maxZ && a.maxZ > b.minZ;
    }

    function createVoxelGrid(sourceBoxes, bounds, cellSize) {
        let sizeX = Math.max(1, Math.ceil((bounds.maxX - bounds.minX) / cellSize));
        let sizeY = Math.max(1, Math.ceil((bounds.maxY - bounds.minY) / cellSize));
        let sizeZ = Math.max(1, Math.ceil((bounds.maxZ - bounds.minZ) / cellSize));
        let adjustedCellSize = cellSize;
        while (sizeX * sizeY * sizeZ > 32768) {
            adjustedCellSize *= 2;
            sizeX = Math.max(1, Math.ceil((bounds.maxX - bounds.minX) / adjustedCellSize));
            sizeY = Math.max(1, Math.ceil((bounds.maxY - bounds.minY) / adjustedCellSize));
            sizeZ = Math.max(1, Math.ceil((bounds.maxZ - bounds.minZ) / adjustedCellSize));
        }
        const data = new Array(sizeX * sizeY * sizeZ).fill(false);
        const index = (x, y, z) => x + sizeX * (y + sizeY * z);
        for (let z = 0; z < sizeZ; z++) {
            for (let y = 0; y < sizeY; y++) {
                for (let x = 0; x < sizeX; x++) {
                    const voxelBox = {
                        minX: bounds.minX + x * adjustedCellSize,
                        minY: bounds.minY + y * adjustedCellSize,
                        minZ: bounds.minZ + z * adjustedCellSize,
                        maxX: Math.min(bounds.maxX, bounds.minX + (x + 1) * adjustedCellSize),
                        maxY: Math.min(bounds.maxY, bounds.minY + (y + 1) * adjustedCellSize),
                        maxZ: Math.min(bounds.maxZ, bounds.minZ + (z + 1) * adjustedCellSize)
                    };
                    data[index(x, y, z)] = sourceBoxes.some(box => intersects(box, voxelBox));
                }
            }
        }
        return {bounds, cellSize: adjustedCellSize, sizeX, sizeY, sizeZ, data, index};
    }

    function isVoxelFilled(grid, visited, x, y, z) {
        if (x < 0 || y < 0 || z < 0 || x >= grid.sizeX || y >= grid.sizeY || z >= grid.sizeZ) {
            return false;
        }
        const index = grid.index(x, y, z);
        return grid.data[index] && !visited[index];
    }

    function canFillBox(grid, visited, startX, startY, startZ, width, height, depth) {
        if (startX + width > grid.sizeX || startY + height > grid.sizeY || startZ + depth > grid.sizeZ) {
            return false;
        }
        for (let z = startZ; z < startZ + depth; z++) {
            for (let y = startY; y < startY + height; y++) {
                for (let x = startX; x < startX + width; x++) {
                    if (!isVoxelFilled(grid, visited, x, y, z)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    function tryGrowBox(grid, visited, startX, startY, startZ, order) {
        const box = {width: 1, height: 1, depth: 1};
        let changed = true;
        while (changed) {
            changed = false;
            for (const axis of order) {
                const next = Object.assign({}, box);
                if (axis === 'x') {
                    next.width++;
                } else if (axis === 'y') {
                    next.height++;
                } else {
                    next.depth++;
                }
                if (canFillBox(grid, visited, startX, startY, startZ, next.width, next.height, next.depth)) {
                    box.width = next.width;
                    box.height = next.height;
                    box.depth = next.depth;
                    changed = true;
                }
            }
        }
        return box;
    }

    function chooseBestBox(grid, visited, startX, startY, startZ) {
        const orders = [
            ['x', 'z', 'y'],
            ['x', 'y', 'z'],
            ['z', 'x', 'y'],
            ['z', 'y', 'x'],
            ['y', 'x', 'z'],
            ['y', 'z', 'x']
        ];
        return orders
            .map(order => tryGrowBox(grid, visited, startX, startY, startZ, order))
            .sort((a, b) => (b.width * b.height * b.depth) - (a.width * a.height * a.depth))[0];
    }

    function markVisited(grid, visited, startX, startY, startZ, width, height, depth) {
        for (let z = startZ; z < startZ + depth; z++) {
            for (let y = startY; y < startY + height; y++) {
                for (let x = startX; x < startX + width; x++) {
                    visited[grid.index(x, y, z)] = true;
                }
            }
        }
    }

    function voxelBoxToModelBox(grid, startX, startY, startZ, width, height, depth) {
        return {
            minX: grid.bounds.minX + startX * grid.cellSize,
            minY: grid.bounds.minY + startY * grid.cellSize,
            minZ: grid.bounds.minZ + startZ * grid.cellSize,
            maxX: Math.min(grid.bounds.maxX, grid.bounds.minX + (startX + width) * grid.cellSize),
            maxY: Math.min(grid.bounds.maxY, grid.bounds.minY + (startY + height) * grid.cellSize),
            maxZ: Math.min(grid.bounds.maxZ, grid.bounds.minZ + (startZ + depth) * grid.cellSize)
        };
    }

    function greedyMergeVoxels(grid) {
        const visited = new Array(grid.data.length).fill(false);
        const boxes = [];
        for (let y = 0; y < grid.sizeY; y++) {
            for (let z = 0; z < grid.sizeZ; z++) {
                for (let x = 0; x < grid.sizeX; x++) {
                    if (!isVoxelFilled(grid, visited, x, y, z)) {
                        continue;
                    }
                    const box = chooseBestBox(grid, visited, x, y, z);
                    markVisited(grid, visited, x, y, z, box.width, box.height, box.depth);
                    boxes.push(voxelBoxToModelBox(grid, x, y, z, box.width, box.height, box.depth));
                }
            }
        }
        return boxes;
    }

    function boxesTouchOrOverlap(a, b, axis) {
        const epsilon = 0.000001;
        if (axis === 'x') {
            return Math.abs(a.maxX - b.minX) < epsilon || Math.abs(b.maxX - a.minX) < epsilon;
        }
        if (axis === 'y') {
            return Math.abs(a.maxY - b.minY) < epsilon || Math.abs(b.maxY - a.minY) < epsilon;
        }
        return Math.abs(a.maxZ - b.minZ) < epsilon || Math.abs(b.maxZ - a.minZ) < epsilon;
    }

    function sameRange(a, b, minKey, maxKey) {
        const epsilon = 0.000001;
        return Math.abs(a[minKey] - b[minKey]) < epsilon && Math.abs(a[maxKey] - b[maxKey]) < epsilon;
    }

    function canMergeBoxes(a, b) {
        return (boxesTouchOrOverlap(a, b, 'x') && sameRange(a, b, 'minY', 'maxY') && sameRange(a, b, 'minZ', 'maxZ'))
            || (boxesTouchOrOverlap(a, b, 'y') && sameRange(a, b, 'minX', 'maxX') && sameRange(a, b, 'minZ', 'maxZ'))
            || (boxesTouchOrOverlap(a, b, 'z') && sameRange(a, b, 'minX', 'maxX') && sameRange(a, b, 'minY', 'maxY'));
    }

    function mergeTwoBoxes(a, b) {
        return {
            minX: Math.min(a.minX, b.minX),
            minY: Math.min(a.minY, b.minY),
            minZ: Math.min(a.minZ, b.minZ),
            maxX: Math.max(a.maxX, b.maxX),
            maxY: Math.max(a.maxY, b.maxY),
            maxZ: Math.max(a.maxZ, b.maxZ)
        };
    }

    function mergeAdjacentBoxes(boxes) {
        let merged = boxes.slice();
        let changed = true;
        while (changed) {
            changed = false;
            outer: for (let i = 0; i < merged.length; i++) {
                for (let j = i + 1; j < merged.length; j++) {
                    if (!canMergeBoxes(merged[i], merged[j])) {
                        continue;
                    }
                    merged[i] = mergeTwoBoxes(merged[i], merged[j]);
                    merged.splice(j, 1);
                    changed = true;
                    break outer;
                }
            }
        }
        return merged;
    }

    function generateSimpleBoxes(sourceBoxes) {
        const bounds = calculateBoundsFromBoxes(sourceBoxes);
        return bounds ? [bounds] : [];
    }

    function generateComplexBoxes(sourceBoxes, simplification, maxBoxes) {
        const bounds = calculateBoundsFromBoxes(sourceBoxes);
        if (!bounds) {
            return [];
        }
        const cellSize = Math.max(0.25, Math.min(8, Number(simplification) || 1));
        const grid = createVoxelGrid(sourceBoxes, bounds, cellSize);
        return mergeAdjacentBoxes(greedyMergeVoxels(grid))
            .sort((a, b) => getBoxVolume(b) - getBoxVolume(a))
            .slice(0, Math.max(1, Math.min(128, Number.parseInt(maxBoxes, 10) || 32)));
    }

    function getUnvisitedFilledCount(grid, visited) {
        let count = 0;
        for (let index = 0; index < grid.data.length; index++) {
            if (grid.data[index] && !visited[index]) {
                count++;
            }
        }
        return count;
    }

    function findLargestCuboidAt(grid, visited, startX, startY, startZ) {
        if (!isVoxelFilled(grid, visited, startX, startY, startZ)) {
            return null;
        }
        let best = {x: startX, y: startY, z: startZ, width: 1, height: 1, depth: 1, volume: 1};
        let maxWidth = 0;
        while (isVoxelFilled(grid, visited, startX + maxWidth, startY, startZ)) {
            maxWidth++;
        }
        for (let width = 1; width <= maxWidth; width++) {
            let maxDepth = 0;
            while (true) {
                const z = startZ + maxDepth;
                if (z >= grid.sizeZ) {
                    break;
                }
                let valid = true;
                for (let x = startX; x < startX + width; x++) {
                    if (!isVoxelFilled(grid, visited, x, startY, z)) {
                        valid = false;
                        break;
                    }
                }
                if (!valid) {
                    break;
                }
                maxDepth++;
            }
            for (let depth = 1; depth <= maxDepth; depth++) {
                let height = 1;
                while (canFillBox(grid, visited, startX, startY, startZ, width, height + 1, depth)) {
                    height++;
                }
                const volume = width * height * depth;
                if (volume > best.volume) {
                    best = {x: startX, y: startY, z: startZ, width, height, depth, volume};
                }
            }
        }
        return best;
    }

    function findGlobalLargestCuboid(grid, visited) {
        let best = null;
        for (let y = 0; y < grid.sizeY; y++) {
            for (let z = 0; z < grid.sizeZ; z++) {
                for (let x = 0; x < grid.sizeX; x++) {
                    if (!isVoxelFilled(grid, visited, x, y, z)) {
                        continue;
                    }
                    const cuboid = findLargestCuboidAt(grid, visited, x, y, z);
                    if (cuboid && (!best || cuboid.volume > best.volume)) {
                        best = cuboid;
                    }
                }
            }
        }
        return best;
    }

    function globalLargestCuboidCover(grid, maxBoxes) {
        const limit = Math.max(1, Math.min(128, Number.parseInt(maxBoxes, 10) || 32));
        const visited = new Array(grid.data.length).fill(false);
        const boxes = [];
        while (boxes.length < limit && getUnvisitedFilledCount(grid, visited) > 0) {
            const cuboid = findGlobalLargestCuboid(grid, visited);
            if (!cuboid) {
                break;
            }
            markVisited(grid, visited, cuboid.x, cuboid.y, cuboid.z, cuboid.width, cuboid.height, cuboid.depth);
            boxes.push(voxelBoxToModelBox(grid, cuboid.x, cuboid.y, cuboid.z, cuboid.width, cuboid.height, cuboid.depth));
        }
        return mergeAdjacentBoxes(boxes).sort((a, b) => getBoxVolume(b) - getBoxVolume(a));
    }

    function generateQualityBoxes(sourceBoxes, simplification, maxBoxes) {
        const bounds = calculateBoundsFromBoxes(sourceBoxes);
        if (!bounds) {
            return [];
        }
        const cellSize = Math.max(0.25, Math.min(8, Number(simplification) || 1));
        const grid = createVoxelGrid(sourceBoxes, bounds, cellSize);
        return globalLargestCuboidCover(grid, maxBoxes);
    }

    function greedyRectangles2D(mask, width, height) {
        const rectangles = [];
        const data = mask.slice();
        let n = 0;
        for (let y = 0; y < height; y++) {
            for (let x = 0; x < width;) {
                if (!data[n]) {
                    x++;
                    n++;
                    continue;
                }
                let rectWidth = 1;
                while (x + rectWidth < width && data[n + rectWidth]) {
                    rectWidth++;
                }
                let rectHeight = 1;
                while (y + rectHeight < height) {
                    let k = 0;
                    while (k < rectWidth && data[n + k + rectHeight * width]) {
                        k++;
                    }
                    if (k < rectWidth) {
                        break;
                    }
                    rectHeight++;
                }
                for (let dy = 0; dy < rectHeight; dy++) {
                    for (let dx = 0; dx < rectWidth; dx++) {
                        data[n + dx + dy * width] = false;
                    }
                }
                rectangles.push({x, y, width: rectWidth, height: rectHeight, area: rectWidth * rectHeight});
                x += rectWidth;
                n += rectWidth;
            }
        }
        return rectangles;
    }

    function buildSliceMask(grid, visited, axis, layer) {
        const u = (axis + 1) % 3;
        const v = (axis + 2) % 3;
        const dims = [grid.sizeX, grid.sizeY, grid.sizeZ];
        const width = dims[u];
        const height = dims[v];
        const mask = new Array(width * height).fill(false);
        for (let vv = 0; vv < height; vv++) {
            for (let uu = 0; uu < width; uu++) {
                const coord = [0, 0, 0];
                coord[axis] = layer;
                coord[u] = uu;
                coord[v] = vv;
                mask[uu + vv * width] = isVoxelFilled(grid, visited, coord[0], coord[1], coord[2]);
            }
        }
        return {mask, width, height, u, v};
    }

    function rectangleCanExtrude(grid, visited, axis, startLayer, rect, u, v, depth) {
        const layer = startLayer + depth;
        const dims = [grid.sizeX, grid.sizeY, grid.sizeZ];
        if (layer >= dims[axis]) {
            return false;
        }
        for (let vv = rect.y; vv < rect.y + rect.height; vv++) {
            for (let uu = rect.x; uu < rect.x + rect.width; uu++) {
                const coord = [0, 0, 0];
                coord[axis] = layer;
                coord[u] = uu;
                coord[v] = vv;
                if (!isVoxelFilled(grid, visited, coord[0], coord[1], coord[2])) {
                    return false;
                }
            }
        }
        return true;
    }

    function markAxisRectVisited(grid, visited, axis, startLayer, rect, u, v, depth) {
        for (let dd = 0; dd < depth; dd++) {
            for (let vv = rect.y; vv < rect.y + rect.height; vv++) {
                for (let uu = rect.x; uu < rect.x + rect.width; uu++) {
                    const coord = [0, 0, 0];
                    coord[axis] = startLayer + dd;
                    coord[u] = uu;
                    coord[v] = vv;
                    visited[grid.index(coord[0], coord[1], coord[2])] = true;
                }
            }
        }
    }

    function axisRectToModelBox(grid, axis, startLayer, rect, u, v, depth) {
        const mins = [0, 0, 0];
        const maxs = [0, 0, 0];
        mins[axis] = startLayer;
        maxs[axis] = startLayer + depth;
        mins[u] = rect.x;
        maxs[u] = rect.x + rect.width;
        mins[v] = rect.y;
        maxs[v] = rect.y + rect.height;
        return voxelBoxToModelBox(grid, mins[0], mins[1], mins[2], maxs[0] - mins[0], maxs[1] - mins[1], maxs[2] - mins[2]);
    }

    function getGreedySliceCandidates(grid, visited) {
        const candidates = [];
        const dims = [grid.sizeX, grid.sizeY, grid.sizeZ];
        for (let axis = 0; axis < 3; axis++) {
            for (let layer = 0; layer < dims[axis]; layer++) {
                const slice = buildSliceMask(grid, visited, axis, layer);
                greedyRectangles2D(slice.mask, slice.width, slice.height).forEach(rect => {
                    let depth = 1;
                    while (rectangleCanExtrude(grid, visited, axis, layer, rect, slice.u, slice.v, depth)) {
                        depth++;
                    }
                    candidates.push({axis, layer, rect, u: slice.u, v: slice.v, depth, volume: rect.area * depth});
                });
            }
        }
        return candidates;
    }

    function greedySliceCover(grid, maxBoxes) {
        const limit = Math.max(1, Math.min(128, Number.parseInt(maxBoxes, 10) || 32));
        const visited = new Array(grid.data.length).fill(false);
        const boxes = [];
        while (boxes.length < limit && getUnvisitedFilledCount(grid, visited) > 0) {
            const candidates = getGreedySliceCandidates(grid, visited);
            if (!candidates.length) {
                break;
            }
            const best = candidates.sort((a, b) => b.volume - a.volume)[0];
            markAxisRectVisited(grid, visited, best.axis, best.layer, best.rect, best.u, best.v, best.depth);
            boxes.push(axisRectToModelBox(grid, best.axis, best.layer, best.rect, best.u, best.v, best.depth));
        }
        return mergeAdjacentBoxes(boxes).sort((a, b) => getBoxVolume(b) - getBoxVolume(a));
    }

    function generateGreedySliceBoxes(sourceBoxes, simplification, maxBoxes) {
        const bounds = calculateBoundsFromBoxes(sourceBoxes);
        if (!bounds) {
            return [];
        }
        const cellSize = Math.max(0.25, Math.min(8, Number(simplification) || 1));
        const grid = createVoxelGrid(sourceBoxes, bounds, cellSize);
        return greedySliceCover(grid, maxBoxes);
    }

    function removeExistingHitboxGroup() {
        if (typeof Group === 'undefined' || !Group.all) {
            return;
        }
        const existing = Group.all.find(group => group.name === 'hitbox');
        if (existing && existing.remove) {
            existing.remove();
        }
    }

    function clearCubeFaces(cube) {
        if (!cube || !cube.faces) {
            return;
        }
        Object.keys(cube.faces).forEach(key => {
            const face = cube.faces[key];
            if (!face) {
                return;
            }
            face.texture = null;
            face.enabled = false;
            if (face.uv) {
                face.uv = [0, 0, 0, 0];
            }
        });
    }

    function createHitboxCubes(boxes) {
        Undo.initEdit({elements: Cube.all.slice(), outliner: true});
        removeExistingHitboxGroup();
        const hitboxGroup = new Group({
            name: 'hitbox',
            origin: [0, 0, 0]
        }).init();
        hitboxGroup.addTo('root');
        boxes.forEach((box, index) => {
            const hitboxCube = new Cube({
                name: boxes.length === 1 ? 'hitbox' : `hitbox_${index + 1}`,
                from: [box.minX, box.minY, box.minZ],
                to: [box.maxX, box.maxY, box.maxZ],
                origin: [0, 0, 0],
                autouv: 0
            }).init();
            clearCubeFaces(hitboxCube);
            hitboxCube.addTo(hitboxGroup);
        });
        hitboxGroup.openUp();
        Canvas.updateAll();
        Undo.finishEdit('Generate MoreBlock hitbox');
    }

    function resolveHitboxMode(value) {
        if (value === 'complex' || value === 'quality' || value === 'greedy') {
            return value;
        }
        return 'simple';
    }

    function generateHitbox(mode, options) {
        const text = getText();
        const sourceBoxes = getSourceCubes().map(getCubeBounds).filter(box => getBoxVolume(box) > 0);
        if (!sourceBoxes.length) {
            Blockbench.showQuickMessage(text.noModelCubes);
            return;
        }
        const simplification = options && options.simplification !== undefined ? options.simplification : 1;
        const maxBoxes = options && options.max_boxes !== undefined ? options.max_boxes : 32;
        let boxes;
        if (mode === 'greedy') {
            boxes = generateGreedySliceBoxes(sourceBoxes, simplification, maxBoxes);
        } else if (mode === 'quality') {
            boxes = generateQualityBoxes(sourceBoxes, simplification, maxBoxes);
        } else if (mode === 'complex') {
            boxes = generateComplexBoxes(sourceBoxes, simplification, maxBoxes);
        } else {
            boxes = generateSimpleBoxes(sourceBoxes);
        }
        if (!boxes.length) {
            Blockbench.showQuickMessage(text.noModelCubes);
            return;
        }
        createHitboxCubes(boxes);
        Blockbench.showQuickMessage(text.hitboxGeneratedWithCount(boxes.length));
    }

    function showHitboxDialog() {
        const text = getText();
        const dialog = new Dialog({
            id: 'moreblock_hitbox_dialog',
            title: text.hitboxDialogTitle,
            width: 520,
            form: {
                mode: {
                    label: text.hitboxMode,
                    type: 'select',
                    options: {
                        simple: text.simpleMode,
                        complex: text.complexMode,
                        quality: text.qualityMode,
                        greedy: text.greedyMode
                    },
                    value: 'simple'
                },
                simplification: {
                    label: text.simplification,
                    type: 'number',
                    value: 1,
                    min: 0.25,
                    max: 8,
                    step: 0.25,
                    description: text.simplificationInfo
                },
                max_boxes: {
                    label: text.maxBoxes,
                    type: 'number',
                    value: 32,
                    min: 1,
                    max: 128,
                    step: 1,
                    description: text.maxBoxesInfo
                }
            },
            onConfirm(form) {
                generateHitbox(resolveHitboxMode(form.mode), form);
            }
        });
        dialog.show();
    }

    function showExportDialog() {
        const text = getText();
        const baseName = getProjectBaseName();
        const defaultId = sanitizeId(baseName);
        const dialog = new Dialog({
            id: 'moreblock_config_export_dialog',
            title: text.dialogTitle,
            width: 560,
            form: {
                id: {
                    label: text.blockId,
                    type: 'text',
                    value: defaultId
                },
                zh_cn: {
                    label: text.chineseName,
                    type: 'text',
                    value: baseName === 'custom_block' ? text.defaultChineseName : baseName
                },
                en_us: {
                    label: text.englishName,
                    type: 'text',
                    value: text.defaultEnglishName
                },
                geo: {
                    label: text.geoFile,
                    type: 'text',
                    value: getDefaultGeoFile()
                },
                texture: {
                    label: text.textureFile,
                    type: 'text',
                    value: 'texture.png'
                },
                display: {
                    label: text.displayFile,
                    type: 'text',
                    value: ''
                },
                light_level: {
                    label: text.lightLevel,
                    type: 'number',
                    value: 0,
                    min: 0,
                    max: 15
                },
                supports_sitting: {
                    label: text.canSit,
                    type: 'checkbox',
                    value: false
                },
                seat_height: {
                    label: text.seatHeight,
                    type: 'number',
                    value: 0.5,
                    min: 0,
                    max: 2,
                    step: 0.05
                }
            },
            onConfirm(form) {
                const latestText = getText();
                const config = buildConfig(form);
                const content = JSON.stringify(config, null, 2) + '\n';
                Blockbench.export({
                    type: latestText.exportType,
                    extensions: ['json'],
                    name: `${config.id}.json`,
                    content
                }, path => {
                    if (path) {
                        Blockbench.showQuickMessage(latestText.exported);
                    }
                });
            }
        });
        dialog.show();
    }

    Plugin.register(PLUGIN_ID, {
        title: getText().pluginTitle,
        author: 'Sallos',
        icon: 'fa-cube',
        description: getText().pluginDescription,
        version: '1.6.0',
        variant: 'both',
        min_version: '4.0.0',
        tags: ['Minecraft: Java Edition'],
        onload() {
            const text = getText();
            moreblock_export_action = new Action('export_moreblock_config_json', {
                name: text.actionName,
                description: text.actionDescription,
                icon: 'fa-cube',
                category: 'file',
                click: showExportDialog
            });
            moreblock_generate_hitbox_action = new Action('generate_moreblock_hitbox', {
                name: text.hitboxActionName,
                description: text.hitboxActionDescription,
                icon: 'select_all',
                category: 'edit',
                click: showHitboxDialog
            });
            MenuBar.addAction(moreblock_export_action, 'file.export');
            MenuBar.addAction(moreblock_generate_hitbox_action, 'filter');
        },
        onunload() {
            if (moreblock_export_action) {
                moreblock_export_action.delete();
                moreblock_export_action = null;
            }
            if (moreblock_generate_hitbox_action) {
                moreblock_generate_hitbox_action.delete();
                moreblock_generate_hitbox_action = null;
            }
        }
    });
})();
