from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from models import Post, Comment, User
from database import db

forum_bp = Blueprint('forum', __name__)


@forum_bp.route('/posts', methods=['GET'])
def get_posts():
    try:
        # 获取查询参数
        page = request.args.get('page', 1, type=int)
        per_page = request.args.get('per_page', 20, type=int)
        category = request.args.get('category', None)

        # 构建查询
        query = Post.query

        # 按类别筛选
        if category:
            query = query.filter(Post.category == category)

        # 按时间降序排列并分页
        posts = query.order_by(Post.created_at.desc()).paginate(
            page=page, per_page=per_page, error_out=False
        )

        # 转换为字典列表
        posts_data = [post.to_dict() for post in posts.items]

        return jsonify({
            'posts': posts_data,
            'total': posts.total,
            'pages': posts.pages,
            'current_page': page
        }), 200

    except Exception as e:
        return jsonify({'error': str(e)}), 500


@forum_bp.route('/posts/<int:post_id>', methods=['GET'])
def get_post(post_id):
    try:
        post = Post.query.get_or_404(post_id)
        return jsonify(post.to_dict()), 200
    except Exception as e:
        return jsonify({'error': str(e)}), 500


@forum_bp.route('/posts', methods=['POST'])
@jwt_required()
def create_post():
    try:
        data = request.get_json()

        # 验证必要字段
        if not data or not data.get('title') or not data.get('content'):
            return jsonify({'error': 'Missing required fields'}), 400

        # 获取当前用户ID
        current_user_id = get_jwt_identity()

        # 创建新帖子
        new_post = Post(
            author_id=current_user_id,
            title=data['title'],
            content=data['content'],
            category=data.get('category', 'General')
        )

        db.session.add(new_post)
        db.session.commit()

        # 返回创建的帖子
        return jsonify(new_post.to_dict()), 201

    except Exception as e:
        db.session.rollback()
        return jsonify({'error': str(e)}), 500


@forum_bp.route('/posts/<int:post_id>/comments', methods=['GET'])
def get_comments(post_id):
    try:
        # 确保帖子存在
        Post.query.get_or_404(post_id)

        # 获取评论
        comments = Comment.query.filter_by(post_id=post_id).order_by(Comment.created_at.asc()).all()
        comments_data = [comment.to_dict() for comment in comments]

        return jsonify(comments_data), 200

    except Exception as e:
        return jsonify({'error': str(e)}), 500


@forum_bp.route('/posts/<int:post_id>/comments', methods=['POST'])
@jwt_required()
def create_comment(post_id):
    try:
        data = request.get_json()

        # 验证必要字段
        if not data or not data.get('content'):
            return jsonify({'error': 'Missing content'}), 400

        # 确保帖子存在
        Post.query.get_or_404(post_id)

        # 获取当前用户ID
        current_user_id = get_jwt_identity()

        # 创建新评论
        new_comment = Comment(
            post_id=post_id,
            author_id=current_user_id,
            content=data['content']
        )

        db.session.add(new_comment)
        db.session.commit()

        return jsonify(new_comment.to_dict()), 201

    except Exception as e:
        db.session.rollback()
        return jsonify({'error': str(e)}), 500


@forum_bp.route('/categories', methods=['GET'])
def get_categories():
    try:
        # 获取所有不重复的类别
        categories = db.session.query(Post.category).distinct().all()
        categories_list = [category[0] for category in categories]

        return jsonify(categories_list), 200

    except Exception as e:
        return jsonify({'error': str(e)}), 500